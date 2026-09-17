# 文档图示与可编辑源文件

需求分析说明书包含 13 处图示，设计说明书包含 10 处图示，复用下列 20 张图片。Word 中已嵌入图片，离线打开也能显示。结构图保留 SVG 源文件，可直接编辑文字、连线与布局。

## 架构与 UML 图

| 图示 | PNG | SVG |
| --- | --- | --- |
| 系统总体架构 | [查看](diagrams/architecture.png) | [源文件](diagrams/architecture.svg) |
| 系统主用例图 | [查看](diagrams/usecase-main.png) | [源文件](diagrams/usecase-main.svg) |
| 员工端用例图 | [查看](diagrams/usecase-employee.png) | [源文件](diagrams/usecase-employee.svg) |
| 管理端用例图 | [查看](diagrams/usecase-admin.png) | [源文件](diagrams/usecase-admin.svg) |
| 文档入库活动图 | [查看](diagrams/flow-ingest.png) | [源文件](diagrams/flow-ingest.svg) |
| 智能问答活动图 | [查看](diagrams/flow-qa.png) | [源文件](diagrams/flow-qa.svg) |
| 系统功能模块图 | [查看](diagrams/modules.png) | [源文件](diagrams/modules.svg) |
| 文档处理核心类图 | [查看](diagrams/class-ingest.png) | [源文件](diagrams/class-ingest.svg) |
| 问答核心类图 | [查看](diagrams/class-chat.png) | [源文件](diagrams/class-chat.svg) |
| 文档入库时序图 | [查看](diagrams/sequence-ingest.png) | [源文件](diagrams/sequence-ingest.svg) |
| 检索增强问答时序图 | [查看](diagrams/sequence-chat.png) | [源文件](diagrams/sequence-chat.svg) |
| 资源访问控制流程 | [查看](diagrams/access.png) | [源文件](diagrams/access.svg) |
| 数据库实体关系图 | [查看](diagrams/er.png) | [源文件](diagrams/er.svg) |

## 页面原型

以下图片来自当前 Vue 页面，使用固定测试数据，并标注“界面原型 · 示例数据”。问答内容、命中率和响应时间用于说明界面，不是模型评测结果。

| 页面 | 图片 |
| --- | --- |
| 登录 | [查看](diagrams/prototype-login.png) |
| 智能问答 | [查看](diagrams/prototype-chat.png) |
| 历史会话 | [查看](diagrams/prototype-history.png) |
| 常见问题入口 | [查看](diagrams/prototype-faq.png) |
| 知识库文档管理 | [查看](diagrams/prototype-kb.png) |
| 用户与权限管理 | [查看](diagrams/prototype-users.png) |
| 问答统计 | [查看](diagrams/prototype-stats.png) |

## 重新生成

1. Python 运行 `scripts/build-diagrams.py`，生成 13 张 SVG 和尺寸清单。
2. 启动前端开发服务器；设置 `PROTOTYPE_URL` 为其地址，默认端口 5181。
3. 设置 `RUNTIME_NODE_MODULES` 指向包含 Playwright 的 Node 模块目录，确保已安装 Chrome，运行 `node scripts/capture-prototypes.mjs`。脚本将 SVG 渲染为 PNG，截取 7 个页面，并检查登录、会话切换、FAQ、单次发送和来源预览。只更新结构图时可加 `--diagrams-only`，无需启动前端。
4. Python 环境安装 `python-docx`，运行 `python scripts/update-documents.py --reference <原始两份Word所在目录>`。原始文件名须为 `系统需求分析说明书.docx` 和 `系统设计说明书.docx`，输出到 `项目文档/`。
5. 将 Word 渲染为 PDF/逐页图片，检查全部页面的图题、中文字体、分页和表格。修改任何图文后重新渲染受影响文档。

原始附件作为本地参考，不纳入仓库；最终文档和全部图源已纳入版本控制。
