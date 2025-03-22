# WorldManager

WorldManager is a Bukkit/Spigot plugin for managing multiple worlds in your Minecraft server. It provides a comprehensive set of commands to create, load, unload, teleport between, and delete worlds. It also features a template system that allows you to create new worlds based on existing templates.

## Features

### Implemented
- **World Creation**: Create new worlds with a single command
- **World Loading/Unloading**: Load and unload worlds to save server resources
- **World Teleportation**: Teleport between different worlds
- **World Deletion**: Delete worlds that are no longer needed
- **World Listing**: View a list of all available worlds
- **Template System**: Create new worlds based on predefined templates
- **Debug Mode**: Enable detailed logging for troubleshooting

### Planned
- **UI Interface**: Graphical user interface for easier world management
- **Internationalization**: Support for multiple languages

## Commands

- `/wm help` - Display help information
- `/wm create <world_name>` - Create a new world
- `/wm ct <template_name> <world_name>` - Create a world from a template
- `/wm ct list` - List all available templates
- `/wm load <world_name>` - Load a world
- `/wm unload <world_name>` - Unload a world
- `/wm tp <world_name>` - Teleport to a world
- `/wm list` - List all worlds
- `/wm del <world_name> confirm` - Delete a world

## Permissions

- `worldmanager.admin` - Access to all WorldManager commands
- `worldmanager.create` - Permission to create new worlds
- `worldmanager.load` - Permission to load worlds
- `worldmanager.unload` - Permission to unload worlds
- `worldmanager.teleport` - Permission to teleport between worlds
- `worldmanager.list` - Permission to view world list
- `worldmanager.delete` - Permission to delete worlds
- `worldmanager.rename` - Permission to rename worlds

## Configuration

The plugin uses a configuration file (`config.yml`) to store settings:

```yaml
# Valid world templates list
templates:
  - 'example_template'
  - 'nether_template'
  - 'end_template'

# Debug mode
debug: false
```

## Template System

The template system allows you to create predefined world templates that can be used to quickly generate new worlds. Templates are stored in the plugin's `templates` directory and must be listed in the configuration file to be recognized.

## Language

[中文文档](README_ZH.md)

## License

This project is open source and available under the [MIT License](LICENSE).