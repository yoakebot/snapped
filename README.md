首先,获取到当前的商品信息,并根据时间进行countdown计时
未开始:继续countdown,结束时调取秒杀地址暴露接口,此时后台会判断时间是否准确并返回结果
	若不准确,继续countdown
	若准确,返回md5地址-->
		然后调用执行秒杀接口:
			-->后台验证md5准确性
			-->查询库存数量和时间是否满足(where id = ? and stock>0 and start<now<end)
			-->执行插入数据(unique key防止重复秒杀)
			-->秒杀结果

			
1）时间轮询获取时间->获取验证码和接口地址->提交后按钮变灰，请求轮询结果
2）验证验证码，限制请求次数->进入秒杀接口->验证是否登入->验证限流拒绝策略->是否重复下单->剩余请求减一（为0开启拒绝策略）->进入mq队列->返回结果轮询
3）进入mq->验证库存->验证是否重复请求->秒杀
4）进入秒杀->乐观锁减库存（无库存缓存标识）->下单->写订单缓存
5）前端轮询结果，查订单缓存->缓存存在则成功下单->不存在查无库存标识符->有库存返回继续轮询->无库存下单失败
