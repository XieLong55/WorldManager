package cn.enderrealm.worldManager.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldManagerUtils {

    /**
     * 检查是否启用了调试模式
     * @return 是否启用调试模式
     */
    private static boolean isDebugEnabled() {
        org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("WorldManager");
        if (plugin == null) {
            return false;
        }
        return plugin.getConfig().getBoolean("debug", false);
    }
    
    /**
     * 输出调试日志
     * @param plugin 插件实例
     * @param message 日志消息
     */
    private static void logDebug(org.bukkit.plugin.Plugin plugin, String message) {
        if (plugin != null && isDebugEnabled()) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    /**
     * 获取插件的templates文件夹
     * @return templates文件夹对象
     */
    private static File getTemplatesFolder() {
        // 获取插件实例
        org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("WorldManager");
        if (plugin == null) {
            return null;
        }
        
        // 获取插件数据文件夹
        File dataFolder = plugin.getDataFolder();
        File templatesFolder = new File(dataFolder, "templates");
        
        // 确保templates文件夹存在
        if (!templatesFolder.exists()) {
            templatesFolder.mkdirs();
        }
        
        return templatesFolder;
    }
    
    /**
     * 获取所有可用的模板名称
     * @return 模板名称列表
     */
    public static List<String> getTemplateNames() {
        List<String> templateNames = new ArrayList<>();
        
        // 获取插件实例用于日志记录
        org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("WorldManager");
        if (plugin == null) {
            return templateNames;
        }
        
        // 获取模板文件夹
        File templatesFolder = getTemplatesFolder();
        if (templatesFolder == null) {
            plugin.getLogger().warning("无法获取模板列表：模板文件夹不存在");
            return templateNames;
        }
        
        // 从配置文件中获取有效的模板列表
        List<String> configTemplates = plugin.getConfig().getStringList("templates");
        if (configTemplates.isEmpty()) {
            plugin.getLogger().warning("无法获取模板列表：配置文件中没有定义有效的模板");
            return templateNames;
        }
        
        logDebug(plugin, "从配置文件中读取到 " + configTemplates.size() + " 个模板配置");
        plugin.getLogger().info("从配置文件中读取到 " + configTemplates.size() + " 个模板配置");
        
        // 检查模板文件夹中的目录是否在配置的有效模板列表中
        if (templatesFolder.exists()) {
            File[] files = templatesFolder.listFiles();
            if (files == null || files.length == 0) {
                plugin.getLogger().warning("无法获取模板列表：模板文件夹为空");
                return templateNames;
            }
            
            for (File file : files) {
                if (file.isDirectory()) {
                    if (configTemplates.contains(file.getName())) {
                        templateNames.add(file.getName());
                        logDebug(plugin, "找到有效模板：" + file.getName());
                    } else {
                        logDebug(plugin, "忽略未配置的模板文件夹：" + file.getName());
                    }
                }
            }
        } else {
            plugin.getLogger().warning("无法获取模板列表：模板文件夹不存在或无法访问");
        }
        
        if (templateNames.isEmpty()) {
            plugin.getLogger().warning("没有找到任何有效的模板");
        } else {
            plugin.getLogger().info("共找到 " + templateNames.size() + " 个有效模板");
        }
        
        return templateNames;
    }
    
    /**
     * 从模板创建世界
     * @param templateName 模板名称
     * @param worldName 世界名称
     * @return 是否成功创建
     */
    public static boolean createWorldFromTemplate(String templateName, String worldName) {
        // 获取插件实例用于日志记录
        org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("WorldManager");
        
        logDebug(plugin, "开始检查创建世界的前置条件");
        
        // 检查世界是否已存在
        World existingWorld = Bukkit.getWorld(worldName);
        if (existingWorld != null) {
            if (plugin != null) plugin.getLogger().info("检测到世界已加载：" + worldName + "，尝试先卸载该世界");
            logDebug(plugin, "尝试卸载已存在的世界：" + worldName);
            
            // 尝试卸载世界
            boolean unloaded = unloadWorld(worldName);
            if (!unloaded) {
                if (plugin != null) plugin.getLogger().warning("无法创建世界：" + worldName + " - 世界已加载且无法卸载");
                return false; // 无法卸载世界
            }
            logDebug(plugin, "成功卸载已存在的世界：" + worldName);
        }
        logDebug(plugin, "检查通过：世界 " + worldName + " 未加载或已成功卸载");
        
        // 检查世界文件夹是否存在
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (worldFolder.exists() && worldFolder.isDirectory()) {
            if (plugin != null) plugin.getLogger().info("检测到世界文件夹已存在：" + worldName + "，尝试删除该文件夹");
            logDebug(plugin, "尝试删除已存在的世界文件夹：" + worldFolder.getAbsolutePath());
            
            // 尝试删除世界文件夹
            try {
                cleanupFolder(worldFolder);
                logDebug(plugin, "成功删除已存在的世界文件夹");
            } catch (Exception e) {
                if (plugin != null) {
                    plugin.getLogger().warning("无法创建世界：" + worldName + " - 世界文件夹已存在且无法删除: " + e.getMessage());
                    logDebug(plugin, "删除世界文件夹失败，错误详情：" + e.toString());
                }
                return false; // 无法删除世界文件夹
            }
        }
        logDebug(plugin, "检查通过：世界文件夹 " + worldFolder.getAbsolutePath() + " 不存在或已成功删除");
        
        // 获取模板文件夹
        File templatesFolder = getTemplatesFolder();
        if (templatesFolder == null) {
            if (plugin != null) plugin.getLogger().severe("无法创建世界：无法获取模板文件夹");
            return false; // 无法获取模板文件夹
        }
        logDebug(plugin, "成功获取模板文件夹：" + templatesFolder.getAbsolutePath());
        
        // 从配置文件中获取有效的模板列表
        List<String> configTemplates = new ArrayList<>();
        if (plugin != null) {
            configTemplates = plugin.getConfig().getStringList("templates");
            if (!configTemplates.contains(templateName)) {
                plugin.getLogger().warning("无法创建世界：模板 " + templateName + " 不在配置文件的有效模板列表中");
                return false; // 模板不在配置列表中
            }
            logDebug(plugin, "检查通过：模板 " + templateName + " 在配置文件的有效模板列表中");
        }
        
        File templateFolder = new File(templatesFolder, templateName);
        if (!templateFolder.exists() || !templateFolder.isDirectory()) {
            if (plugin != null) plugin.getLogger().severe("无法创建世界：模板文件夹 " + templateName + " 不存在");
            return false; // 模板不存在
        }
        logDebug(plugin, "检查通过：模板文件夹 " + templateFolder.getAbsolutePath() + " 存在");
        
        if (plugin != null) plugin.getLogger().info("开始从模板 " + templateName + " 创建世界: " + worldName);
        
        // 复制模板文件夹到世界文件夹
        try {
            logDebug(plugin, "开始复制模板文件夹到世界文件夹");
            copyFolder(templateFolder, worldFolder);
            
            // 删除uid.dat文件，避免世界被识别为副本
            File uidFile = new File(worldFolder, "uid.dat");
            if (uidFile.exists()) {
                if (uidFile.delete()) {
                    if (plugin != null) plugin.getLogger().info("成功删除uid.dat文件，避免世界被识别为副本");
                    logDebug(plugin, "成功删除uid.dat文件：" + uidFile.getAbsolutePath());
                } else {
                    if (plugin != null) plugin.getLogger().warning("无法删除uid.dat文件，可能导致世界加载失败");
                    logDebug(plugin, "无法删除uid.dat文件：" + uidFile.getAbsolutePath());
                }
            }
            
            if (plugin != null) plugin.getLogger().info("成功复制模板文件夹到世界文件夹");
            logDebug(plugin, "复制完成：从 " + templateFolder.getAbsolutePath() + " 到 " + worldFolder.getAbsolutePath());
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().severe("复制模板文件夹时发生错误: " + e.getMessage());
                logDebug(plugin, "复制失败，错误详情：" + e.toString());
                e.printStackTrace();
            }
            // 清理已创建的文件夹
            try {
                logDebug(plugin, "开始清理失败的世界文件夹");
                cleanupFolder(worldFolder);
                logDebug(plugin, "清理完成");
            } catch (Exception ex) {
                if (plugin != null) {
                    plugin.getLogger().warning("清理失败的世界文件夹时发生错误: " + ex.getMessage());
                    logDebug(plugin, "清理失败，错误详情：" + ex.toString());
                }
            }
            return false; // 复制失败
        }
        
        // 加载世界
        if (plugin != null) plugin.getLogger().info("尝试加载新创建的世界: " + worldName);
        logDebug(plugin, "开始加载新创建的世界");
        boolean result = loadWorld(worldName);
        if (!result && plugin != null) {
            plugin.getLogger().severe("无法加载新创建的世界: " + worldName);
            logDebug(plugin, "加载世界失败");
        } else {
            logDebug(plugin, "成功加载世界 " + worldName);
        }
        return result;
    }
    
    /**
     * 清理文件夹及其内容
     * @param folder 要清理的文件夹
     * @throws Exception 清理过程中的异常
     */
    private static void cleanupFolder(File folder) throws Exception {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        cleanupFolder(file);
                    } else {
                        file.delete();
                    }
                }
            }
            folder.delete();
        }
    }
    
    /**
     * 复制文件夹及其内容
     * @param source 源文件夹
     * @param target 目标文件夹
     * @throws Exception 复制过程中的异常
     */
    private static void copyFolder(File source, File target) throws Exception {
        if (!target.exists()) {
            target.mkdirs();
        }
        
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                File destFile = new File(target, file.getName());
                if (file.isDirectory()) {
                    copyFolder(file, destFile);
                } else {
                    copyFile(file, destFile);
                }
            }
        }
    }
    
    /**
     * 复制文件
     * @param source 源文件
     * @param target 目标文件
     * @throws Exception 复制过程中的异常
     */
    private static void copyFile(File source, File target) throws Exception {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(source);
             java.io.FileOutputStream fos = new java.io.FileOutputStream(target)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }
    
    /**
     * 创建一个新的世界
     * @param worldName 世界名称
     * @return 是否成功创建
     */
    public static boolean createWorld(String worldName) {
        // 检查世界是否已存在
        if (Bukkit.getWorld(worldName) != null) {
            return false; // 世界已存在
        }
        
        // 检查世界文件夹是否存在
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (worldFolder.exists() && worldFolder.isDirectory()) {
            return false; // 世界文件夹已存在
        }
        
        // 创建新世界
        WorldCreator creator = new WorldCreator(worldName);
        World world = creator.createWorld();
        
        return world != null;
    }
    
    /**
     * 加载一个已存在的世界
     * @param worldName 世界名称
     * @return 是否成功加载
     */
    public static boolean loadWorld(String worldName) {
        // 获取插件实例用于日志记录
        org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("WorldManager");
        
        logDebug(plugin, "开始检查加载世界的前置条件：" + worldName);
        
        // 检查世界是否已加载
        if (Bukkit.getWorld(worldName) != null) {
            if (plugin != null) plugin.getLogger().warning("无法加载世界：" + worldName + " - 世界已加载");
            return false; // 世界已加载
        }
        logDebug(plugin, "检查通过：世界 " + worldName + " 未加载");
        
        // 检查世界文件夹是否存在
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists() || !worldFolder.isDirectory()) {
            if (plugin != null) plugin.getLogger().warning("无法加载世界：" + worldName + " - 世界文件夹不存在");
            return false; // 世界文件夹不存在
        }
        logDebug(plugin, "检查通过：世界文件夹存在 " + worldFolder.getAbsolutePath());
        
        // 检查level.dat文件是否存在
        File levelDat = new File(worldFolder, "level.dat");
        if (!levelDat.exists() || !levelDat.isFile()) {
            if (plugin != null) plugin.getLogger().warning("无法加载世界：" + worldName + " - level.dat文件不存在");
            return false; // level.dat文件不存在
        }
        logDebug(plugin, "检查通过：level.dat文件存在");
        
        if (plugin != null) plugin.getLogger().info("开始加载世界: " + worldName);
        
        try {
            // 加载世界
            logDebug(plugin, "创建WorldCreator对象");
            WorldCreator creator = new WorldCreator(worldName);
            logDebug(plugin, "调用createWorld()方法");
            World world = creator.createWorld();
            
            if (world == null) {
                if (plugin != null) plugin.getLogger().severe("加载世界失败：" + worldName + " - 创建世界返回null");
                logDebug(plugin, "加载失败：createWorld()返回null");
                return false;
            }
            
            if (plugin != null) plugin.getLogger().info("成功加载世界: " + worldName);
            logDebug(plugin, "世界加载成功，环境类型：" + world.getEnvironment() + "，种子：" + world.getSeed());
            return true;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().severe("加载世界时发生异常: " + e.getMessage());
                logDebug(plugin, "加载世界时发生异常：" + e.toString());
                e.printStackTrace();
            }
            return false;
        }
    }
    
    /**
     * 卸载一个世界
     * @param worldName 世界名称
     * @return 是否成功卸载
     */
    public static boolean unloadWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        
        // 检查世界是否已加载
        if (world == null) {
            return false; // 世界未加载
        }
        
        // 将该世界中的玩家传送到主世界
        World mainWorld = Bukkit.getWorlds().get(0); // 获取主世界
        for (Player player : world.getPlayers()) {
            player.teleport(mainWorld.getSpawnLocation());
        }
        
        // 卸载世界
        return Bukkit.unloadWorld(world, true);
    }
    
    /**
     * 删除一个世界
     * @param worldName 世界名称
     * @return 是否成功删除
     */
    public static boolean deleteWorld(String worldName) {
        // 先卸载世界
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            if (!unloadWorld(worldName)) {
                return false; // 卸载失败
            }
        }
        
        // 删除世界文件夹
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists() || !worldFolder.isDirectory()) {
            return false; // 世界文件夹不存在
        }
        
        try {
            // 递归删除文件夹
            return deleteFolder(worldFolder);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 递归删除文件夹及其内容
     * @param folder 要删除的文件夹
     * @return 是否成功删除
     */
    private static boolean deleteFolder(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolder(file);
                    } else {
                        file.delete();
                    }
                }
            }
            return folder.delete();
        }
        return false;
    }
    
    /**
     * 将玩家传送到指定世界
     * @param player 玩家
     * @param worldName 世界名称
     * @return 是否成功传送
     */
    public static boolean teleportToWorld(Player player, String worldName) {
        World world = Bukkit.getWorld(worldName);
        
        // 检查世界是否已加载
        if (world == null) {
            return false; // 世界未加载
        }
        
        // 传送玩家
        return player.teleport(world.getSpawnLocation());
    }
    
    /**
     * 获取所有已加载的世界名称
     * @return 世界名称列表
     */
    public static List<String> getLoadedWorldNames() {
        List<String> worldNames = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            worldNames.add(world.getName());
        }
        return worldNames;
    }
    
    /**
     * 获取所有可用的世界名称（包括未加载的）
     * @return 世界名称列表
     */
    public static List<String> getAllWorldNames() {
        List<String> worldNames = new ArrayList<>(getLoadedWorldNames());
        
        // 获取世界容器目录中的所有文件夹
        File worldContainer = Bukkit.getWorldContainer();
        File[] files = worldContainer.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && new File(file, "level.dat").exists()) {
                    String worldName = file.getName();
                    if (!worldNames.contains(worldName)) {
                        worldNames.add(worldName);
                    }
                }
            }
        }
        
        return worldNames;
    }
    
    /**
     * 重命名世界
     * @param oldWorldName 原世界名称
     * @param newWorldName 新世界名称
     * @return 是否成功重命名
     */
    public static boolean renameWorld(String oldWorldName, String newWorldName) {
        // 获取插件实例用于日志记录
        org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("WorldManager");
        
        logDebug(plugin, "开始重命名世界：" + oldWorldName + " -> " + newWorldName);
        
        // 检查新世界名称是否已存在
        World existingNewWorld = Bukkit.getWorld(newWorldName);
        if (existingNewWorld != null) {
            if (plugin != null) plugin.getLogger().warning("无法重命名世界：目标世界名称 " + newWorldName + " 已存在");
            return false;
        }
        
        File newWorldFolder = new File(Bukkit.getWorldContainer(), newWorldName);
        if (newWorldFolder.exists() && newWorldFolder.isDirectory()) {
            if (plugin != null) plugin.getLogger().warning("无法重命名世界：目标世界文件夹 " + newWorldName + " 已存在");
            return false;
        }
        
        // 检查原世界是否存在
        World oldWorld = Bukkit.getWorld(oldWorldName);
        if (oldWorld == null) {
            // 原世界未加载，检查文件夹是否存在
            File oldWorldFolder = new File(Bukkit.getWorldContainer(), oldWorldName);
            if (!oldWorldFolder.exists() || !oldWorldFolder.isDirectory() || !new File(oldWorldFolder, "level.dat").exists()) {
                if (plugin != null) plugin.getLogger().warning("无法重命名世界：源世界 " + oldWorldName + " 不存在");
                return false;
            }
        } else {
            // 原世界已加载，需要先卸载
            if (plugin != null) plugin.getLogger().info("源世界已加载，尝试卸载：" + oldWorldName);
            if (!unloadWorld(oldWorldName)) {
                if (plugin != null) plugin.getLogger().warning("无法重命名世界：无法卸载源世界 " + oldWorldName);
                return false;
            }
            if (plugin != null) plugin.getLogger().info("成功卸载源世界：" + oldWorldName);
        }
        
        // 获取原世界文件夹
        File oldWorldFolder = new File(Bukkit.getWorldContainer(), oldWorldName);
        
        try {
            // 复制原世界文件夹到新世界文件夹
            if (plugin != null) plugin.getLogger().info("开始复制世界文件夹：" + oldWorldName + " -> " + newWorldName);
            copyFolder(oldWorldFolder, newWorldFolder);
            
            // 删除uid.dat文件，避免世界被识别为副本
            File uidFile = new File(newWorldFolder, "uid.dat");
            if (uidFile.exists()) {
                if (uidFile.delete()) {
                    if (plugin != null) plugin.getLogger().info("成功删除新世界的uid.dat文件");
                } else {
                    if (plugin != null) plugin.getLogger().warning("无法删除新世界的uid.dat文件，可能导致世界加载问题");
                }
            }
            
            // 删除原世界文件夹
            if (plugin != null) plugin.getLogger().info("开始删除原世界文件夹：" + oldWorldName);
            deleteFolder(oldWorldFolder);
            
            // 加载新世界
            if (plugin != null) plugin.getLogger().info("开始加载新世界：" + newWorldName);
            boolean loaded = loadWorld(newWorldName);
            if (!loaded) {
                if (plugin != null) plugin.getLogger().severe("无法加载重命名后的世界：" + newWorldName);
                return false;
            }
            
            if (plugin != null) plugin.getLogger().info("成功重命名世界：" + oldWorldName + " -> " + newWorldName);
            return true;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().severe("重命名世界时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }
    
    /**
     * 重新加载插件配置文件
     * @return 是否成功重载
     */
    public static boolean reloadConfig() {
        try {
            // 获取插件实例
            org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("WorldManager");
            if (plugin == null) {
                return false;
            }
            
            // 重新加载配置文件
            plugin.reloadConfig();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}