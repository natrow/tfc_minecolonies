#!/usr/bin/env nu

# Repository for each mod
def repo_path [name] {
    match $name {
        minecolonies | structurize | blockui | domum_ornamentum => $"https://ldtteam.jfrog.io/artifactory/mods-maven/com/ldtteam/($name)/maven-metadata.xml",
        jei => $"https://maven.blamejared.com/mezz/jei/jei-1.20.1-forge/maven-metadata.xml"
        patchouli => $"https://maven.blamejared.com/vazkii/patchouli/Patchouli/maven-metadata.xml"
        terrafirmacraft => $"https://api.modrinth.com/v2/project/($name)/version"
        _ => (error make {msg: $"Unknown repository for mod ($name)"})
    }
}

# Resolve version from mod repository
def resolve [name] {
    let repo = repo_path $name
    if ($repo | str ends-with 'maven-metadata.xml') {(
        http get $repo |
        get content |
        get 2 |
        get content |
        get 2 |
        get content |
        last |
        get content |
        get 0 |
        get content
    )} else if ($repo | str starts-with 'https://api.modrinth.com') {(
        http get $repo |
        get version_number |
        sort |
        last
    )} else {
        error make {msg: $"Unknown repository path ($repo) for mod ($name)"}
    }
}

# print the latest version of each mod
def get_latest_version [name] {
    let version = resolve $name
    print $"Got ($name) version ($version)"
    #$version
}

# all mods to check for
let mods = [jei minecolonies structurize blockui domum_ornamentum terrafirmacraft patchouli]

$mods | each { |it| get_latest_version $it }

null
