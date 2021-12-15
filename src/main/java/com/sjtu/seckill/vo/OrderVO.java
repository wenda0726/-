package com.sjtu.seckill.vo;

import com.sjtu.seckill.pojo.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderVO {
    private Order order;
    private GoodsVO goodsVO;

}
