package cn.enderrealm.worldManager;

import cn.enderrealm.worldManager.commands.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class WorldManager extends JavaPlugin {

    @Override
    public void onEnable() {
        // 保存默认配置文件
        saveDefaultConfig();
        
        // 确保templates文件夹存在
        File templatesFolder = new File(getDataFolder(), "templates");
        if (!templatesFolder.exists()) {
            templatesFolder.mkdirs();
            getLogger().info("已创建templates文件夹: " + templatesFolder.getAbsolutePath());
        }
        
        // 检查配置文件中的模板列表
        if (getConfig().getStringList("templates").isEmpty()) {
            getLogger().warning("配置文件中没有定义有效的模板列表，请检查config.yml");
        }
        
        // Plugin startup logic
        getCommand("worldmanager").setExecutor(new CommandManager());
        getCommand("worldmanager").setTabCompleter(new CommandManager());
        getLogger().info("WorldManager插件已启用，使用 /wm 查看帮助");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
