package com.sjtu.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sjtu.seckill.pojo.Goods;
import com.sjtu.seckill.vo.GoodsVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wendasu
 * @since 2021-12-04
 */
public interface IGoodsService extends IService<Goods> {

    List<GoodsVO> findGoodsList();

    GoodsVO findGoodsByGoodsId(Long goodsId);
}
