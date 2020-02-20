# VRChatNotificationBot
VRChat好友上线提醒DiscordBot

# 安装
1. 克隆本项目
2. 填写`docker-compose.yml`的以下四行
```
        VRCHAT_USERNAME: #VRChat的登录用户名或邮箱(非Steam帐号)
        VRCHAT_PASSWORD: #VRChat的登录密码
        BOT_TOKEN:       #Bot的token 获取地址https://discordapp.com/developers/applications/
        BOT_OWNERID:     #设置->外观->开发者模式 在线列表->自己头像右键->复制ID
```
3. `docker-compose up -d`

# 使用
- `!add|update` 命令 - 在一个Channel添加或更新订阅  
    参数: `[username] [@discordUser] [mask 0-63]   `  
    举例：假设好友名为 Lucy a1b2 （检索基于正则[\s\S]\*keyword[\s\S]\* 大小写不敏感）
    - 用法一：`!add`                            默认使用Channel名作为好友检索的关键字
    - 用法二：`!add lucy`                       使用lucy为好友检索的关键字
    - 用法三：`!add ^lucy$`                     如果存在同名帐号Lucy,可加上匹配首尾
    - 用法四：`!add mask 1`                     使用Channel名检索，同时直接设置只显示上线消息
    - 用法五：`!add @届かない恋`                 设置消息@提醒
    - 用法六：`!add lucy @届かない恋 mask 1`     设置该好友上线时@届かない恋
    - 用法七：`!add * mask 6`                   在该Channel设置显示所有好友的上线下消息
    - 用法八：`!add * @届かない恋 mask 32`       在该Channel设置显示所有好友的Invite、FriendRequest提醒
    - PS: mask值的定义可使用showconfig命令查看
- `!manage` - 显示一个菜单(上限10条，未完善)，用于删除已订阅的好友
- `!showconfig` - 显示当前Channel已订阅好友的配置及mask数值定义
- `!showuser` - 显示好友当前状态  
  参数：`[username]`    
  举例：假设好友名为 Lucy a1b2
  - 用法一：`!showuser lucy`                  显示好友 Lucy a1b2 的在线状态
  - 用法二：`!showuser`                       显示该Channel的所有已订阅用户的在线状态
- `!showonline` - 显示在线好友
- `!showip` - 显示当前服务器的IP地址
- `!restart` - 重启Bot
- `!help` - 命令使用说明