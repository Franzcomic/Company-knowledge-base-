# 星云智联科技有限公司 GitLab 使用指南

| 版本 | 维护部门 |
| --- | --- |
| V1.0 | 运维部 / 研发中心 |

---

## 一、GitLab 概览

公司使用自建 GitLab 托管源代码与团队协作，研发、测试相关人员日常开发均围绕 GitLab 进行。

## 二、账号注册 / 激活

- 新员工账号由 IT/管理员统一开通，通过工作邮箱激活。
- 激活链接会在企业邮箱中查收，点击后设置密码即完成。

## 三、密码忘记 / 重置

- **GitLab 密码忘记时**：
  1. 点击登录页「Forgot your password?」。
  2. 输入注册邮箱，系统发送重置链接。
  3. 通过工作邮箱打开链接设置新密码。
- 若工作邮箱也无法进入，联系管理员重置。

## 四、SSH Key 配置

1. 本地生成密钥：`ssh-keygen -t rsa -b 4096 -C "yourname@xxxx.com"`。
2. 查看公钥：`cat ~/.ssh/id_rsa.pub`。
3. 在 GitLab「Preferences → SSH Keys」粘贴公钥并保存。
4. 此后可通过 SSH 免密 clone/push。

## 五、项目创建 / 克隆 / 权限

- **克隆**：`git clone git@gitlab.xxx.com:group/project.git`（SSH）或使用 HTTP 地址。
- **创建项目**：New project → 填写名称、可见性、分组。
- **权限**：按角色（Guest/Reporter/Developer/Maintainer/Owner）分配，由 Maintainer/Owner 设置。

## 六、常见问题

| 问题 | 处理 |
| --- | --- |
| clone 报权限错误 | 确认已配置 SSH Key 并加入项目组 |
| push 被拒绝 | 检查分支权限与当前分支保护规则 |
| 页面加载慢/失败 | 确认网络，必要时连 VPN |

## 联系我们
- 通过 OA 提交 IT 工单或联系管理员。

---

## 相关文档
- 《06-02 Git使用规范》《05-01 VPN使用指南》