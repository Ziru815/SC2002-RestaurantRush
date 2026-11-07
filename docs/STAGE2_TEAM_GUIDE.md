# Stage 2 四人分工与接口约定

Stage 2 已实现并整合。默认启动保留 Stage 1；`sh scripts/run.sh --stage2` 启动混合顾客游戏。
本页记录实际职责和固定接口；提交记录保留每个人的历史，整合修改不会改写作者。

| 部分 | 主要职责 | 测试入口 |
| --- | --- | --- |
| A / Ziru | VIPCustomer、CriticCustomer；复用 Customer 状态规则 | Stage2CustomerTests：6 场景 |
| B | Order 创建时锁定顾客报价 | Stage2OrderTests：8 场景 |
| C | 员工验证和三类顾客完整流程 | Stage2StaffTests：3 组场景 |
| D / Tianren | 到达工厂、控制器接线、阶段显示、启动参数与整合 | Stage2ControllerTests：10 场景 |

## 固定接口

| 调用 | 约定 |
| --- | --- |
| `Customer.waitingLoss(): int` | Regular 8、VIP 5、Critic 12；父类只在 QUEUED/SEATED 扣分并截到 0 |
| `Customer.priceFor(MenuItem): int` | 以 cents 返回顾客折扣后的报价；普通/Critic 原价，VIP 九折；null 拒绝；无状态修改 |
| `Customer.chooseMeal(Menu): MenuItem` | Regular/VIP 保留 ID 循环；Critic 固定选 Pasta；null 拒绝 |
| `Customer.getTypeLabel(): String` | Regular / VIP / Critic，仅供显示，不能用字符串分支实现游戏规则 |
| `Order(Customer, MenuItem)` | 构造时调用一次 `priceFor`，保存 final lockedPriceCents |
| `Cashier.collectPayment(Customer, int)` | 只取订单锁定金额，不重新报价、不二次折扣 |
| `Stage2Arrivals.create(int id): Customer` | 校验 1..6；ID 2 为 VIP，ID 4 为 Critic，其余 Regular |

**组内设计选择**：Critic 固定选 Pasta，ID 2/4 替换为 VIP/Critic。普通 4 号按原有循环会选 Salad，Critic 4 号则选 Pasta，因此在基础运行里能直接看到偏好的差异。这两点是组内选定的可复现方案，不是老师强制的具体菜品/顺序。VIP 若出现非整分价格，采用四舍五入到分（half-up），中间乘法用 long；三种基础菜单的九折本身都恰好是整数分。

## 验证与后续协作

- `sh scripts/test.sh` 编译并运行全部 9 个测试套件，共 72 项检查。
- `sh scripts/test-stage2.sh` 是同一测试入口的兼容别名，不重复运行套件。
- 胜败输入位于 `src/test/resources/stage2-*.input.txt`；输出记录位于 `docs/evidence/stage2/`。
- 接线接口为 `GameController(Restaurant, ConsoleUI, IntFunction<Customer> arrivalFactory)` 与 `ConsoleUI(InputStream, PrintStream, String stageLabel)`；原有两参数构造器保留默认 Stage 1。
- PR 使用 Create a merge commit；不 rebase、不强推，不能合并自己发起的 PR。
- 保留完整 AI 使用历史、实际测试记录和个人反思；每人须能解释自己的设计和测试。
