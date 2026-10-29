# Stage 1 四人分工与接入说明

这份骨架已经把类、构造函数和调用入口接起来，但还没有实现完整游戏。先由四人认领 A/B/C/D，分别填写负责的方法；D 负责把各部分接成回合流程，其他成员一起排查整合问题。角色代号不是人名，也不表示工作量恰好相等。

## 分工

| 角色 | 负责文件（都在 src/main/java/restaurantrush） | 要写什么 | 完成后如何证明 |
| --- | --- | --- | --- |
| A 顾客与菜单 | Customer.java, RegularCustomer.java, CustomerState.java, Menu.java, MenuItem.java | 顾客选菜、状态变化、满意度、吃饭和付款资格；菜单初始值和读取已提供 | 排队每轮掉 8；服务后不掉；不能当轮吃完付款；ID 1/2/3 分别选 Salad/Burger/Pasta |
| B 桌子与订单 | Table.java, Order.java, OrderStatus.java | 入座/释放桌子；准备进度；READY/SERVED/PAID/CANCELLED 状态保护 | Pasta 要两次准备；做好当轮不能上菜；付款/离开前不能释放桌子 |
| C 员工 | Staff.java, Waiter.java, Chef.java, Cashier.java | 调用 A/B 方法执行动作；每人每轮一次；付款记账及释放桌子 | 不合法动作不耗任务；厨师一次只做一单位；同一账单不能付两次 |
| D 主循环和整合 | Restaurant.java, GameController.java, ConsoleUI.java, Main.java, GameConfig.java | 六次到店、18 回合、输入重试、按顺序调用 A/B/C、胜负；界面启动和预览已提供 | 完整走通顾客流程；到 18 回合才结束；能重复胜利和失败 |

D 的工作依赖其他三人的接口，不能等最后一天才整合。其他人先交一个小功能，D 当天就接入并运行。需要 Codex 帮忙时，把最新分支或 ZIP 和具体报错提供出来。

## 已固定的接口

所有文件用同一个 `package restaurantrush;`。暂时不要自行改类名、方法名、参数顺序、返回值或包名；确实要改，先告诉四个人，并把调用处和类图一起改。

| 发起方 → 接收方 | 调用和约定 |
| --- | --- |
| D → C | `waiter.seat(Customer, Table)` 返回 void；成功后 D `removeFromQueue(customer)` |
| C → B/A | 入座先统一校验，再 `table.seat(customer)`，接着 `customer.seat(table)`，最后 `markActed()` |
| D → C | `waiter.takeOrder(Customer, Menu)` 返回 Order；成功后 D `addOrder(order)` 并显示顾客选的菜 |
| C → A/B | `customer.chooseMeal(menu)` → `new Order(customer, item)` → `customer.placeOrder(order)` |
| D → C | `chef.advance(Order, int turn)`；同轮刚点的订单允许准备 |
| C → B | `order.advance(turn)`；一次只增加一个准备单位 |
| D → C | `waiter.serve(Order, int turn)`；成功后 D `recordServed()` |
| C → B/A | `order.isServable(turn)` 为真后 `order.markServed()` → `customer.markServed(turn)` |
| D → A | 每轮付款阶段调用 `customer.finishEating(turn)`，再用 `customer.canPay(turn)` 过滤 |
| D → C | `cashier.collectPayment(Customer, int turn)` 返回收费的整数分；D 移除已付的 active order |
| C → B/A | 保存旧 Table 引用 → order.markPaid → customer.markPaid → table.release；记录金额和付款时满意度 |
| D → A/B | 满意度降到 0 后 `Restaurant.handleDeparture(customer)` 统一协调取消订单、离开和释放桌子 |

Table 和 Customer 各自维护自己的关联，C/D 统一协调两边；不要在两个 `seat()` 方法里互相调用，否则可能递归。所有前置校验要在第一处状态修改之前完成。只有整个员工动作成功，才消耗任务。

菜单价格/锁定账单/收入的单位都是 cents。例如 `1800` 表示 $18.00，不是 $1800。界面负责显示两位小数。

## 依次整合

1. A/B 先实现并检查状态转换；暂时可在自己测试里直接构造顾客和订单。
2. C 接入 A/B，检查每轮一次动作和无效动作不耗任务。
3. D 先接一位顾客，从到店走到付款。然后实现六次到店和完整 18 回合。
4. 全组执行 STAGE1_ACCEPTANCE.md，记录命令输入和实际输出，修正类图并打 `stage-1` 标签。

每个回合的顺序：增加回合数和重置员工 → 到店 → 显示状态 → 服务员行动 → 厨师行动 → 吃饭和自动收款 → 扣满意度和离开 → 显示回合结果。第 18 回合全部处理完后才检查最终结果。

## 现在怎么操作 GitHub

骨架在 `feature/stage1-skeleton`，并有 draft PR。组里先 review 后合并这个骨架到 main，再分别从更新后的 main 建 `work/customer-menu`、`work/table-order`、`work/staff`、`work/controller` 分支。每个人只修改自己认领的文件；测试文件也可以各建自己的文件，避免四个人同时改一个测试类。

每次提交前：`sh scripts/test.sh`。成员实现业务方法后，要把对应的“仍是 TODO”的烟雾测试替换为真实业务测试；不能把生产代码重新改成 TODO 来维持旧测试通过。

整合时把各 PR 合到同一个分支再测试，不能只保证各自文件单独看着正确。使用 GitHub PR 的差异检查和提交历史；不要反复覆盖上传整份 ZIP。

## 课堂上可以展示什么

运行 `sh scripts/run.sh`，展示配置、菜单、status 和 tasks，解释四人的边界；明确说明这是 skeleton，start 尚未实现。完整 gameplay、victory/defeat 和 `stage-1` 标签仍然待完成。
