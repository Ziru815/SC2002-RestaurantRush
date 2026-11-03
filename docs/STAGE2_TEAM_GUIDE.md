# Stage 2 四人分工与接口约定

这是可编译的接力框架，不是 Stage 2 成品。默认启动仍运行 Stage 1；新的业务方法会明确抛出 TODO 异常，不返回伪造结果。先读本页，再看自己的代码标记。

## 先做什么

1. 由 **Tianren 或 C** 审查 `work/stage2-scaffold -> main` 的框架 PR；Ziru 不合并自己的 PR。使用 **Create a merge commit**，不要 squash/rebase。
2. 四个成员分支已从同一份框架创建。框架合入 main 后，再在下面的分支提交自己的实现；PR 都以 main 为目标。不要把四个分支互相串成依赖链。
3. A/B 可以并行完成。C/D 可以先写测试和接线，但集成验收依赖 A/B。建议合并 A -> B -> C -> D；依赖已合入时，在自己的分支 `git merge origin/main`，不 rebase、不强推。

| 角色 | 分支 | 独占主要文件 |
| --- | --- | --- |
| A / Ziru | work/stage2-customer | VIPCustomer.java, CriticCustomer.java, Customer.java；Stage2CustomerTests.java |
| B / 原订单桌子负责人 | work/stage2-order | Order.java；Stage2OrderTests.java |
| C | work/stage2-staff | Stage2StaffTests.java；有真实缺陷才改 Staff/Waiter/Chef/Cashier |
| D / Tianren | work/stage2-controller | Stage2Arrivals.java, GameController.java, Main.java, ConsoleUI.java；Stage2ControllerTests.java，测试脚本和整合记录 |

Java 文件均在 `src/main/java/restaurantrush/` 或 `src/test/java/restaurantrush/`。B 的姓名由组里认领；不推断或伪造作者。不要为了平均行数复制业务逻辑；测试、调试、评审和设计说明都是贡献。

## 固定接口

| 调用 | 约定 |
| --- | --- |
| `Customer.waitingLoss(): int` | Regular 8、VIP 5、Critic 12；父类只在 QUEUED/SEATED 扣分并截到 0 |
| `Customer.priceFor(MenuItem): int` | 以 cents 返回顾客折扣后的报价；普通/Critic 原价，VIP 九折；null 拒绝；无状态修改 |
| `Customer.chooseMeal(Menu): MenuItem` | Regular/VIP 保留 ID 循环；Critic 固定选 Pasta；null 拒绝 |
| `Customer.getTypeLabel(): String` | Regular / VIP / Critic，仅供显示，不能用字符串分支实现游戏规则 |
| `Order(Customer, MenuItem)` | B 在构造时调用一次 `priceFor`，保存 final lockedPriceCents |
| `Cashier.collectPayment(Customer, int)` | 只取订单锁定金额，不重新报价、不二次折扣 |
| `Stage2Arrivals.create(int id): Customer` | D 校验 1..6；ID 2 为 VIP，ID 4 为 Critic，其余 Regular |

**组内设计选择**：Critic 固定选 Pasta，ID 2/4 替换为 VIP/Critic。普通 4 号按原有循环会选 Salad，Critic 4 号则选 Pasta，因此在基础运行里能直接看到偏好的差异。这两点是本框架选定的可复现方案，不是老师强制的具体菜品/顺序。VIP 若出现非整分价格，采用四舍五入到分（half-up），中间乘法用 long；三种基础菜单的九折本身都恰好是整数分。

## A：顾客行为

- [ ] 写 `Stage2CustomerTests`：初始 100；等待一轮后 Regular 92、VIP 95、Critic 88；多轮不能低于 0；服务后不再扣分。
- [ ] 填 VIP 的 `waitingLoss()` 和 `priceFor()`，Critic 的 `waitingLoss()` 和 `chooseMeal()`。
- [ ] 报价：VIP Salad 810、Burger 1350、Pasta 1620；Regular/Critic 原价；菜单价格不变；测试 null 输入和金额边界。
- [ ] 明确测试 RegularCustomer(4) 选 Salad、CriticCustomer(4) 选 Pasta；用不同 Critic ID 证明都选择 Pasta；Regular/VIP 的旧循环不变；父类引用也能调用不同子类行为。
- [ ] `sh scripts/test.sh` 后运行 `java -cp out/main:out/test restaurantrush.Stage2CustomerTests`。
- [ ] 提交自己能解释的代码、测试和顾客设计说明；更新 Stage 2 类图中的继承和操作。

## B：订单报价

- [ ] 在测试里证明 `new Order(new VIPCustomer(2), burger)` 的账单为 1350，其他两类为 1500。
- [ ] 改 `Order` 中的 TODO 行，只在构造时调用一次 `customer.priceFor(item)`；后续 getter/付款绝不重算。
- [ ] 用计数/可变报价的测试 Customer 子类验证报价只调用一次、之后变化不改已创建订单；测试辅助类只放测试目录。
- [ ] 保留 preparation units、READY turn、取消和支付保护；没有必要改 Table。
- [ ] `sh scripts/test.sh` 和 `java -cp out/main:out/test restaurantrush.Stage2OrderTests` 通过；写价格责任与锁价理由。

## C：员工流程验收

- [ ] 把 `Stage2StaffTests` 的占位 main 换成真实场景，依次验证 Regular/VIP/Critic 的 seat -> order -> cook -> serve -> finishEating -> pay。
- [ ] 断言 VIP 最终收入等于锁定金额，满意度按付款时记录，订单与顾客均 PAID，桌子释放。
- [ ] 为三类顾客测试：占用桌入座失败且任务不消耗；未 READY 上菜失败；重复付款不重复加收入。
- [ ] 测试 Pasta 两次烹饪，READY 当轮不允许上菜，刚 READY_TO_PAY 当轮不允许付款，员工每回合一次。
- [ ] 用 `resetTurn()` 模拟不同回合；不能让测试靠绕过真实员工流程通过。
- [ ] `sh scripts/test.sh` 和 `java -cp out/main:out/test restaurantrush.Stage2StaffTests` 通过；保留真实失败/修复证据。

C 的主要交付是测试代码和验收；生产员工类本来就接收 Customer，若无需修改就是复用成功，不用凑改动。

## D：到达、界面和整合

- [ ] 实现 Stage2Arrivals：六个 ID，2 号 VIP、4 号 Critic，其余 Regular；拒绝 0/7，所有人初始 100。
- [ ] 为 GameController 增加可注入的 `IntFunction<Customer>` 到达构造器；保留旧两参数构造器默认 `RegularCustomer::new`。改 processArrivals 调用构造器，不按子类判断扣分/折扣。
- [ ] Main 增加 `--stage2` 选择 `Stage2Arrivals::create`；默认、--check、--help 保持兼容，help 说明新选项。
- [ ] ConsoleUI 支持准确的阶段标题，并显示顾客类型；使用重载构造器等兼容方式保留现有调用，UI 不参与计算。
- [ ] 写 Stage2ControllerTests：六次到达、类型显示、18 回合、非法输入重试、未付款不记收入、两种结局。
- [ ] 实现完成后让正常 `scripts/test.sh` 也运行四个 Stage 2 suites；调整 `test-stage2.sh` 避免互相递归或重复执行。
- [ ] 保存实际 Stage 2 胜败输入/输出。旧 Stage 1 输入未必适合 Critic 的较快扣分和 Pasta 偏好，不能直接复制旧结果。
- [ ] 更新 README 中 1->2 的实际文件差异及理由、Stage 2 类图与 sequence diagram；全组验收后打 stage-2。

## Git 操作（每人替换成自己的分支）

```sh
git fetch origin --tags
git switch --track origin/work/stage2-customer
# 若本地已存在，使用 git switch work/stage2-customer。
# 依赖 PR 合入 main 后：
git fetch origin
git merge origin/main
# 完成自己的修改后，选择具体文件提交，不要覆盖整个项目。
sh scripts/test.sh
git add <your-files>
git commit -m "Implement Stage 2 customer behavior and tests"
git push -u origin work/stage2-customer
```

每人用自己的账户和真实身份提交，开自己的 PR；另一人审查和 merge commit 合并。保留完整 AI 使用历史、实际测试记录和自己写的反思。每人能在视频中解释自己的设计和至少一个测试。
