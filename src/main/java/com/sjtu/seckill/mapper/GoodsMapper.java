package com.sjtu.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sjtu.seckill.pojo.Goods;
import com.sjtu.seckill.vo.GoodsVO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wendasu
 * @since 2021-12-04
 */
@Repository
public interface GoodsMapper extends BaseMapper<Goods> {

    List<GoodsVO> findGoodsList();

    GoodsVO findGoodsByGoodsId(Long goodsId);
}
