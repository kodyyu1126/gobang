# 五子棋 Web 应用 (Gobang Game)

这是一个基于Spring Boot的五子棋Web应用，支持用户注册、登录、对战、保存棋局等功能。项目采用了现代Java Web技术栈，提供了完整的五子棋游戏体验。

## ✨ 主要特性
- 🎮 支持人人对战和人机对战模式
- 👥 用户注册与登录系统
- 💾 棋局保存与历史记录查看
- 👀 观战模式：实时观看他人对局
- 🔒 安全的用户认证与授权
- 🛠️ 管理员后台管理功能

## 📋 环境要求
- JDK 17+
- MySQL 8.0+
- Maven 3.6+
- RabbitMQ (用于观战功能)


## 🚀 快速开始

### 数据库初始化
在启动项目之前，需要先初始化数据库：
1. 确保MySQL服务已启动
2. 执行项目中的`src/main/resources/db/init-database.sql`脚本
3. 使用命令行初始化数据库
```bash
mysql -u root -p < src/main/resources/db/init-database.sql
```

### 项目启动
1. 克隆项目到本地
```bash
git clone https://github.com/kodyyu1126/gobang.git
cd gobang
```
2. 配置数据库连接（如需修改）
   编辑 `src/main/resources/application.properties` 文件
3. 使用Maven构建并启动项目
```bash
mvn spring-boot:run
```
4. 访问应用
   浏览器中打开 http://localhost:8080


## 🔐 默认账户
- 管理员账户：admin / 123456
- 普通用户账户：user / 123456


## 🧩 技术栈
- **后端**: Spring Boot, Spring Security, MyBatis-Plus
- **前端**: Thymeleaf, JavaScript, CSS
- **数据库**: MySQL
- **消息队列**: RabbitMQ (用于实时观战功能)
- **缓存**: Caffeine
- **数据迁移**: Flyway

## 📂 项目结构
```
src/main/
├── java/com/example/demo/
│   ├── config/       # 配置类
│   ├── controller/   # 控制器
│   ├── mapper/       # MyBatis 映射器
│   ├── model/        # 实体类
│   ├── security/     # 安全相关
│   ├── service/      # 业务逻辑
│   └── util/         # 工具类
└── resources/
    ├── db/           # 数据库脚本
    ├── static/       # 静态资源
    ├── templates/    # Thymeleaf 模板
    └── application.properties  # 应用配置
```

## 🤝 贡献
欢迎贡献代码、报告问题或提出新功能建议！请遵循以下步骤：
1. Fork 本项目
2. 创建你的特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交你的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 打开一个 Pull Request

## 📝 开源协议
本项目采用 [MIT 许可证](LICENSE)。

## 👨‍💻 关于作者
如果你喜欢这个项目，可以在GitHub上关注我 [@kodyyu1126](https://github.com/kodyyu1126)。
邮箱联系：kodyyu1126@outlook.com 
