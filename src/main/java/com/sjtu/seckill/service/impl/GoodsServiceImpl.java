package com.sjtu.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sjtu.seckill.mapper.GoodsMapper;
import com.sjtu.seckill.pojo.Goods;
import com.sjtu.seckill.service.IGoodsService;
import com.sjtu.seckill.vo.GoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wendasu
 * @since 2021-12-04
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {
    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public List<GoodsVO> findGoodsList() {
        return goodsMapper.findGoodsList();
    }

    @Override
    public GoodsVO findGoodsByGoodsId(Long goodsId) {
        return goodsMapper.findGoodsByGoodsId(goodsId);
    }
}
