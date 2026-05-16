#!/usr/bin/env ruby
# Adds dev/stg/prod build configurations + shared schemes to the iOS project.
# Idempotent: re-running should converge to the same state.

require "xcodeproj"
require "fileutils"

PROJECT_PATH = File.expand_path("../iosApp/kmpstarter/kmpstarter.xcodeproj", __dir__)
PROJECT_DIR  = File.dirname(PROJECT_PATH)
CONFIGS_DIR  = File.join(PROJECT_DIR, "Configs")

# Environment name -> xcconfig filename (relative to PROJECT_DIR)
ENV_TO_XCCONFIG = {
  "Dev"  => "Configs/Config-Dev.xcconfig",
  "Stg"  => "Configs/Config-Stg.xcconfig",
  "Prod" => "Configs/Config-Prod.xcconfig",
}

# Each new configuration is named "<BuildType>-<Env>" and is based on either Debug or Release.
NEW_CONFIGS = [
  { name: "Debug-Dev",   type: :debug,   env: "Dev"  },
  { name: "Debug-Stg",   type: :debug,   env: "Stg"  },
  { name: "Debug-Prod",  type: :debug,   env: "Prod" },
  { name: "Release-Dev", type: :release, env: "Dev"  },
  { name: "Release-Stg", type: :release, env: "Stg"  },
  { name: "Release-Prod",type: :release, env: "Prod" },
]

project = Xcodeproj::Project.open(PROJECT_PATH)

# ─────────────────────────────────────────────────────────────────────────────
# 1. Add Configs/ group + xcconfig file references (idempotent)
# ─────────────────────────────────────────────────────────────────────────────
configs_group = project.main_group["Configs"] || project.main_group.new_group("Configs", "Configs")

xcconfig_refs = {}
ENV_TO_XCCONFIG.each do |env, rel_path|
  filename = File.basename(rel_path)
  ref = configs_group.files.find { |f| f.path == filename }
  ref ||= configs_group.new_file(filename)
  xcconfig_refs[env] = ref
end

# Also add Config-Common.xcconfig so it appears in the project tree.
unless configs_group.files.any? { |f| f.path == "Config-Common.xcconfig" }
  configs_group.new_file("Config-Common.xcconfig")
end

# ─────────────────────────────────────────────────────────────────────────────
# 2. Add new configurations at the project level
# ─────────────────────────────────────────────────────────────────────────────
NEW_CONFIGS.each do |cfg|
  existing = project.build_configurations.find { |c| c.name == cfg[:name] }
  if existing
    existing.build_settings["KOTLIN_FRAMEWORK_BUILD_TYPE"] = cfg[:type].to_s
    next
  end
  template_name = (cfg[:type] == :debug) ? "Debug" : "Release"
  template = project.build_configurations.find { |c| c.name == template_name }
  new_cfg = project.new(Xcodeproj::Project::Object::XCBuildConfiguration)
  new_cfg.name = cfg[:name]
  new_cfg.build_settings = template.build_settings.dup
  new_cfg.build_settings["KOTLIN_FRAMEWORK_BUILD_TYPE"] = cfg[:type].to_s
  new_cfg.base_configuration_reference = xcconfig_refs[cfg[:env]]
  project.root_object.build_configuration_list.build_configurations << new_cfg
end

# ─────────────────────────────────────────────────────────────────────────────
# 3. Add matching configurations at the target level + update bundle id
# ─────────────────────────────────────────────────────────────────────────────
project.targets.each do |target|
  NEW_CONFIGS.each do |cfg|
    existing = target.build_configurations.find { |c| c.name == cfg[:name] }
    if existing
      existing.build_settings["KOTLIN_FRAMEWORK_BUILD_TYPE"] = cfg[:type].to_s
      next
    end
    template_name = (cfg[:type] == :debug) ? "Debug" : "Release"
    template = target.build_configurations.find { |c| c.name == template_name }
    new_cfg = project.new(Xcodeproj::Project::Object::XCBuildConfiguration)
    new_cfg.name = cfg[:name]
    new_cfg.build_settings = template.build_settings.dup
    new_cfg.build_settings["KOTLIN_FRAMEWORK_BUILD_TYPE"] = cfg[:type].to_s
    new_cfg.base_configuration_reference = xcconfig_refs[cfg[:env]]
    target.build_configuration_list.build_configurations << new_cfg
  end

  # Make PRODUCT_BUNDLE_IDENTIFIER honor BUNDLE_ID_SUFFIX from xcconfig.
  target.build_configurations.each do |c|
    c.build_settings["PRODUCT_BUNDLE_IDENTIFIER"] = "com.dignicate.kmpstarter$(BUNDLE_ID_SUFFIX)"
  end
end

# ─────────────────────────────────────────────────────────────────────────────
# 4. Attach Config-Prod.xcconfig to the original Debug/Release so they build prod.
# ─────────────────────────────────────────────────────────────────────────────
%w[Debug Release].each do |name|
  cfg = project.build_configurations.find { |c| c.name == name }
  cfg.base_configuration_reference = xcconfig_refs["Prod"] if cfg && cfg.base_configuration_reference.nil?
end
project.targets.each do |target|
  %w[Debug Release].each do |name|
    cfg = target.build_configurations.find { |c| c.name == name }
    cfg.base_configuration_reference = xcconfig_refs["Prod"] if cfg && cfg.base_configuration_reference.nil?
  end
end

project.save

# ─────────────────────────────────────────────────────────────────────────────
# 5. Create shared schemes for dev/stg/prod
# ─────────────────────────────────────────────────────────────────────────────
shared_data_dir = File.join(PROJECT_PATH, "xcshareddata", "xcschemes")
FileUtils.mkdir_p(shared_data_dir)

target = project.targets.first
ENV_TO_XCCONFIG.keys.each do |env|
  scheme_name = "kmpstarter-#{env.downcase}"
  scheme_path = File.join(shared_data_dir, "#{scheme_name}.xcscheme")

  scheme = Xcodeproj::XCScheme.new
  scheme.add_build_target(target)
  scheme.set_launch_target(target)

  scheme.launch_action.build_configuration   = "Debug-#{env}"
  scheme.test_action.build_configuration     = "Debug-#{env}"
  scheme.analyze_action.build_configuration  = "Debug-#{env}"
  scheme.profile_action.build_configuration  = "Release-#{env}"
  scheme.archive_action.build_configuration  = "Release-#{env}"

  scheme.save_as(PROJECT_PATH, scheme_name, true)
end

puts "Done. Configurations and schemes set up."
project.build_configurations.each { |c| puts "  proj cfg: #{c.name} -> #{c.base_configuration_reference&.path || "(none)"}" }
