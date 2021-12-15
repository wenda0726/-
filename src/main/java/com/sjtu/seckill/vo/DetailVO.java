package com.sjtu.seckill.vo;

import com.sjtu.seckill.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailVO {
    private User user;
    private GoodsVO goodsVO;
    private int secKillStatus;
    private int remainSeconds;
}
