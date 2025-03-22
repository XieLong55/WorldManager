# WorldManager 世界管理器

WorldManager 是一个 Bukkit/Spigot 插件，用于管理 Minecraft 服务器中的多个世界。它提供了一套完整的命令来创建、加载、卸载、传送和删除世界。它还具有模板系统，允许您基于现有模板创建新世界。

## 功能特性

### 已实现
- **世界创建**：使用单个命令创建新世界
- **世界加载/卸载**：加载和卸载世界以节省服务器资源
- **世界传送**：在不同世界之间传送
- **世界删除**：删除不再需要的世界
- **世界列表**：查看所有可用世界的列表
- **模板系统**：基于预定义模板创建新世界
- **调试模式**：启用详细日志记录以进行故障排除

### 计划实现
- **UI界面**：图形用户界面，使世界管理更加容易
- **国际化**：支持多种语言

## 命令

- `/wm help` - 显示帮助信息
- `/wm create <世界名称>` - 创建新世界
- `/wm ct <模板名称> <世界名称>` - 从模板创建世界
- `/wm ct list` - 列出所有可用模板
- `/wm load <世界名称>` - 加载世界
- `/wm unload <世界名称>` - 卸载世界
- `/wm tp <世界名称>` - 传送到世界
- `/wm list` - 列出所有世界
- `/wm del <世界名称> confirm` - 删除世界

## 权限

- `worldmanager.admin` - 访问所有 WorldManager 命令
- `worldmanager.create` - 创建新世界的权限
- `worldmanager.load` - 加载世界的权限
- `worldmanager.unload` - 卸载世界的权限
- `worldmanager.teleport` - 在世界之间传送的权限
- `worldmanager.list` - 查看世界列表的权限
- `worldmanager.delete` - 删除世界的权限
- `worldmanager.rename` - 重命名世界的权限

## 配置

插件使用配置文件 (`config.yml`) 存储设置：

```yaml
# 有效的世界模板列表
templates:
  - 'example_template'
  - 'nether_template'
  - 'end_template'

# 调试模式
debug: false
```

## 模板系统

模板系统允许您创建预定义的世界模板，可用于快速生成新世界。模板存储在插件的 `templates` 目录中，并且必须在配置文件中列出才能被识别。

## 语言

[English Documentation](README.md)

## 许可证

本项目是开源的，可在 [MIT 许可证](LICENSE) 下使用。