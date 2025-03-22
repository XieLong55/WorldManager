package cn.enderrealm.worldManager.commands;

import cn.enderrealm.worldManager.world.WorldManagerUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("worldmanager")) {
            if (args.length == 0) {
                // 无参数时显示帮助信息
                sendHelpMessage(sender);
                return true;
            }
            
            String subCommand = args[0].toLowerCase();
            
            // 处理help子命令
            if (subCommand.equals("help")) {
                sendHelpMessage(sender);
                return true;
            }
            
            // 检查基本权限
            if (!sender.hasPermission("worldmanager.admin")) {
                sender.sendMessage("§c你没有权限使用此命令！");
                return true;
            }
            
            // 处理create-template子命令（别名ct）
            if (subCommand.equals("create-template") || subCommand.equals("ct")) {
                if (!sender.hasPermission("worldmanager.create")) {
                    sender.sendMessage("§c你没有权限创建世界！");
                    return true;
                }
                
                // 处理ct list子命令
                if (args.length > 1 && args[1].equalsIgnoreCase("list")) {
                    List<String> templates = WorldManagerUtils.getTemplateNames();
                    sender.sendMessage("§6========== §e可用的世界模板 §6==========");
                    if (templates.isEmpty()) {
                        sender.sendMessage("§7没有可用的世界模板");
                        sender.sendMessage("§7请确保在config.yml中配置了有效的模板，并且templates文件夹中存在对应的模板文件夹");
                    } else {
                        for (String templateName : templates) {
                            sender.sendMessage("§a- " + templateName);
                        }
                    }
                    sender.sendMessage("§6=================================");
                    return true;
                }
                
                if (args.length < 3) {
                    sender.sendMessage("§c用法: /wm ct <模板名称> <世界名称> 或 /wm ct list");
                    return true;
                }
                
                String templateName = args[1];
                String worldName = args[2];
                
                // 检查模板是否存在于配置文件中
                List<String> templates = WorldManagerUtils.getTemplateNames();
                if (!templates.contains(templateName)) {
                    sender.sendMessage("§c模板 '" + templateName + "' 不存在或未在配置文件中定义");
                    sender.sendMessage("§c请使用 /wm ct list 查看可用的模板");
                    return true;
                }
                
                sender.sendMessage("§e正在从模板 '" + templateName + "' 创建世界: '" + worldName + "'，请稍候...");
                
                if (WorldManagerUtils.createWorldFromTemplate(templateName, worldName)) {
                    sender.sendMessage("§a成功从模板 " + templateName + " 创建世界: " + worldName);
                } else {
                    sender.sendMessage("§c从模板创建世界失败，可能是以下原因：");
                    sender.sendMessage("§c1. 模板 '" + templateName + "' 不存在或文件夹结构不正确");
                    sender.sendMessage("§c2. 世界 '" + worldName + "' 已存在或名称无效");
                    sender.sendMessage("§c3. 服务器没有足够的权限创建或复制文件");
                    sender.sendMessage("§c请检查服务器控制台获取更详细的错误信息");
                }
                return true;
            }
            
            // 处理create子命令
            if (subCommand.equals("create")) {
                if (!sender.hasPermission("worldmanager.create")) {
                    sender.sendMessage("§c你没有权限创建世界！");
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /wm create <世界名称>");
                    return true;
                }
                
                String worldName = args[1];
                if (WorldManagerUtils.createWorld(worldName)) {
                    sender.sendMessage("§a成功创建世界: " + worldName);
                } else {
                    sender.sendMessage("§c创建世界失败，该世界可能已存在");
                }
                return true;
            }
            
            // 处理load子命令
            if (subCommand.equals("load")) {
                if (!sender.hasPermission("worldmanager.load")) {
                    sender.sendMessage("§c你没有权限加载世界！");
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /wm load <世界名称>");
                    return true;
                }
                
                String worldName = args[1];
                if (WorldManagerUtils.loadWorld(worldName)) {
                    sender.sendMessage("§a成功加载世界: " + worldName);
                } else {
                    sender.sendMessage("§c加载世界失败，该世界可能不存在或已加载");
                }
                return true;
            }
            
            // 处理unload子命令
            if (subCommand.equals("unload")) {
                if (!sender.hasPermission("worldmanager.unload")) {
                    sender.sendMessage("§c你没有权限卸载世界！");
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /wm unload <世界名称>");
                    return true;
                }
                
                String worldName = args[1];
                if (WorldManagerUtils.unloadWorld(worldName)) {
                    sender.sendMessage("§a成功卸载世界: " + worldName);
                } else {
                    sender.sendMessage("§c卸载世界失败，该世界可能不存在或无法卸载");
                }
                return true;
            }
            
            // 处理del子命令
            if (subCommand.equals("del")) {
                if (!sender.hasPermission("worldmanager.delete")) {
                    sender.sendMessage("§c你没有权限删除世界！");
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /wm del <世界名称>");
                    return true;
                }
                
                String worldName = args[1];
                
                // 检查是否有confirm参数
                if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
                    sender.sendMessage("§c你真的要删除这个世界吗？他会消失很久很久，真的很久");
                    sender.sendMessage("§c删除请使用: /wm del " + worldName + " confirm");
                    return true;
                }
                
                if (WorldManagerUtils.deleteWorld(worldName)) {
                    sender.sendMessage("§a成功删除世界: " + worldName);
                } else {
                    sender.sendMessage("§c删除世界失败，该世界可能不存在或无法删除");
                }
                return true;
            }
            
            // 处理tp子命令
            if (subCommand.equals("tp")) {
                if (!sender.hasPermission("worldmanager.teleport")) {
                    sender.sendMessage("§c你没有权限传送到其他世界！");
                    return true;
                }
                
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /wm tp <世界名称>");
                    return true;
                }
                
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§c只有玩家才能使用此命令");
                    return true;
                }
                
                Player player = (Player) sender;
                String worldName = args[1];
                
                if (WorldManagerUtils.teleportToWorld(player, worldName)) {
                    player.sendMessage("§a已传送至世界: " + worldName);
                } else {
                    player.sendMessage("§c传送失败，目标世界可能不存在或未加载");
                }
                return true;
            }
            
            // 处理list子命令
            if (subCommand.equals("list")) {
                if (!sender.hasPermission("worldmanager.list")) {
                    sender.sendMessage("§c你没有权限查看世界列表！");
                    return true;
                }
                
                List<String> loadedWorlds = WorldManagerUtils.getLoadedWorldNames();
                sender.sendMessage("§6========== §e已加载的世界 §6==========");
                if (loadedWorlds.isEmpty()) {
                    sender.sendMessage("§7没有已加载的世界");
                } else {
                    for (String worldName : loadedWorlds) {
                        sender.sendMessage("§a- " + worldName);
                    }
                }
                sender.sendMessage("§6=================================");
                return true;
            }
            
            // 处理reload子命令
            if (subCommand.equals("reload")) {
                if (!sender.hasPermission("worldmanager.reload")) {
                    sender.sendMessage("§c你没有权限重载配置文件！");
                    return true;
                }
                
                if (WorldManagerUtils.reloadConfig()) {
                    sender.sendMessage("§a配置文件已成功重载");
                } else {
                    sender.sendMessage("§c重载配置文件失败");
                }
                return true;
            }
            
            // 处理rename子命令
            if (subCommand.equals("rename")) {
                if (!sender.hasPermission("worldmanager.rename")) {
                    sender.sendMessage("§c你没有权限重命名世界！");
                    return true;
                }
                
                if (args.length < 3) {
                    sender.sendMessage("§c用法: /wm rename <原世界名称> <新世界名称>");
                    return true;
                }
                
                String oldWorldName = args[1];
                String newWorldName = args[2];
                
                sender.sendMessage("§e正在重命名世界: '" + oldWorldName + "' -> '" + newWorldName + "'，请稍候...");
                
                if (WorldManagerUtils.renameWorld(oldWorldName, newWorldName)) {
                    sender.sendMessage("§a成功重命名世界: " + oldWorldName + " -> " + newWorldName);
                } else {
                    sender.sendMessage("§c重命名世界失败，可能是以下原因：");
                    sender.sendMessage("§c1. 源世界 '" + oldWorldName + "' 不存在或无法卸载");
                    sender.sendMessage("§c2. 目标世界名称 '" + newWorldName + "' 已存在");
                    sender.sendMessage("§c3. 服务器没有足够的权限创建或复制文件");
                    sender.sendMessage("§c请检查服务器控制台获取更详细的错误信息");
                }
                return true;
            }
            
            // 未知命令
            sender.sendMessage("§c未知命令，请使用 /wm help 查看帮助");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 添加所有子命令
            completions.add("help");
            completions.add("create");
            completions.add("create-template");
            completions.add("ct");
            completions.add("load");
            completions.add("unload");
            completions.add("tp");
            completions.add("list");
            completions.add("reload");
            completions.add("del");
            completions.add("rename");
            
            // 根据玩家输入过滤
            return filterCompletions(completions, args[0]);
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            // 为create-template命令提供模板名称补全
            if (subCommand.equals("create-template") || subCommand.equals("ct")) {
                if (args.length == 2 && "list".startsWith(args[1].toLowerCase())) {
                    completions.add("list");
                    return filterCompletions(completions, args[1]);
                }
                return filterCompletions(WorldManagerUtils.getTemplateNames(), args[1]);
            }
            // 为load、unload和tp命令提供世界名称补全
            else if (subCommand.equals("load")) {
                // 对于load命令，提供所有可用但未加载的世界
                List<String> loadedWorlds = WorldManagerUtils.getLoadedWorldNames();
                List<String> allWorlds = WorldManagerUtils.getAllWorldNames();
                allWorlds.removeAll(loadedWorlds);
                return filterCompletions(allWorlds, args[1]);
            } else if (subCommand.equals("unload") || subCommand.equals("tp")) {
                // 对于unload和tp命令，提供所有已加载的世界
                return filterCompletions(WorldManagerUtils.getLoadedWorldNames(), args[1]);
            } else if (subCommand.equals("del") || subCommand.equals("rename")) {
                // 对于del和rename命令，提供所有可用的世界
                return filterCompletions(WorldManagerUtils.getAllWorldNames(), args[1]);
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            
            // 为create-template命令的第三个参数提供建议（新世界名称）
            if (subCommand.equals("create-template") || subCommand.equals("ct")) {
                // 这里可以提供一些建议，比如基于模板名称的建议
                // 但为了简单起见，我们不提供具体建议，只返回空列表
                return completions;
            } else if (subCommand.equals("del")) {
                // 为del命令的第三个参数提供confirm补全
                completions.add("confirm");
                return filterCompletions(completions, args[2]);
            }
        }
        
        return completions;
    }
    
    /**
     * 根据玩家输入过滤补全列表
     * @param completions 补全列表
     * @param input 玩家输入
     * @return 过滤后的补全列表
     */
    private List<String> filterCompletions(List<String> completions, String input) {
        if (input.isEmpty()) {
            return completions;
        }
        
        String lowerInput = input.toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(lowerInput))
                .collect(Collectors.toList());
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§6========== §e世界管理器帮助 §6==========");
        sender.sendMessage("§e/wm §7- 显示帮助信息");
        sender.sendMessage("§e/wm help §7- 显示帮助信息");
        sender.sendMessage("§e/wm create <世界名称> §7- 创建一个新的世界");
        sender.sendMessage("§e/wm ct <模板名称> <世界名称> §7- 从模板创建世界");
        sender.sendMessage("§e/wm ct list §7- 列出所有可用的世界模板");
        sender.sendMessage("§e/wm load <世界名称> §7- 加载一个已存在的世界");
        sender.sendMessage("§e/wm unload <世界名称> §7- 卸载一个已加载的世界");
        sender.sendMessage("§e/wm del <世界名称> confirm §7- 删除一个世界");
        sender.sendMessage("§e/wm rename <原世界名称> <新世界名称> §7- 重命名世界");
        sender.sendMessage("§e/wm tp <世界名称> §7- 传送到指定世界");
        sender.sendMessage("§e/wm list §7- 列出所有已加载的世界");
        sender.sendMessage("§e/wm reload §7- 重新加载配置文件");
        sender.sendMessage("§6=================================");
    }
}