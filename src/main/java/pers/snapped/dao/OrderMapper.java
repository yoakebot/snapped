package pers.snapped.dao;

import org.apache.ibatis.annotations.*;
import pers.snapped.model.OrderInfo;
import pers.snapped.model.SnappedOrder;

/**
 * ▓██   ██▓ ▒█████   ▄▄▄       ██ ▄█▀▓█████
 * ▒██  ██▒▒██▒  ██▒▒████▄     ██▄█▒ ▓█   ▀
 * ▒██ ██░▒██░  ██▒▒██  ▀█▄  ▓███▄░ ▒███
 * ░ ▐██▓░▒██   ██░░██▄▄▄▄██ ▓██ █▄ ▒▓█  ▄
 * ░ ██▒▓░░ ████▓▒░ ▓█   ▓██▒▒██▒ █▄░▒████▒
 * ██▒▒▒ ░ ▒░▒░▒░  ▒▒   ▓▒█░▒ ▒▒ ▓▒░░ ▒░ ░
 * ▓██ ░▒░   ░ ▒ ▒░   ▒   ▒▒ ░░ ░▒ ▒░ ░ ░  ░
 * ▒ ▒ ░░  ░ ░ ░ ▒    ░   ▒   ░ ░░ ░    ░
 * ░ ░         ░ ░        ░  ░░  ░      ░  ░
 * ░ ░
 */
@Mapper
public interface OrderMapper {

    @Select("SELECT * FROM snapped_order WHERE user_id =#{id}  AND goods_id = #{goodsId}")
    SnappedOrder getOrderByUserIdAndGoodId(@Param("id") Long id, @Param("goodsId") long goodsId);

    @Update("UPDATE snapped_goods SET stock_count = stock_count - 1 WHERE goods_id = #{goodsId} and stock_count > 0 AND NOW()>start_date AND NOW()<end_date")
    int subStock(@Param("goodsId") long goodsId);

    @Insert("INSERT INTO order_info(user_id,goods_id,goods_name,goods_price,order_status,create_date)  " +
            "values(#{userId},#{goodsId},#{goodsName},#{goodsPrice},#{orderStatus},#{createDate})")
    @SelectKey(keyColumn="id", keyProperty="id", resultType=long.class, before=false, statement="select last_insert_id()")
    void saveOrderInfo(OrderInfo orderInfo);

    @Insert("INSERT INTO snapped_order(user_id,order_id,goods_id) values(#{userId},#{orderId}, #{goodsId})")
    void saveSnappedOrder(SnappedOrder snappedOrder);
}
