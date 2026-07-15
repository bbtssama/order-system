package com.atguigu.order.service.impl;

import com.atguigu.order.bean.Product;
import com.atguigu.order.common.BusinessException;
import com.atguigu.order.common.PageResult;
import com.atguigu.order.common.R;
import com.atguigu.order.mapper.ProductMapper;
import com.atguigu.order.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品服务实现（含 Redis 缓存）
 * <p>
 * 缓存策略：
 * <ul>
 *   <li>查询商品详情时优先读缓存，缓存未命中则查库并回写缓存</li>
 *   <li>对不存在的商品缓存空值（占位符），防止缓存穿透</li>
 *   <li>更新和删除商品时主动清除对应缓存</li>
 *   <li>缓存过期时间 30 分钟，保证数据最终一致性</li>
 * </ul>
 * </p>
 */
@Service
public class ProductServiceImpl implements ProductService {

    /** Redis 缓存 key 前缀 */
    private static final String PRODUCT_CACHE_KEY = "product:";
    /** 缓存空值占位符，防止缓存穿透 */
    private static final String NULL_CACHE_VALUE = "NULL";
    /** 缓存过期时间（分钟） */
    private static final long CACHE_TTL = 30;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据ID查询商品详情（带缓存）
     * <p>
     * 缓存穿透防护：当数据库中不存在该商品时，缓存空值占位符，
     * 避免恶意请求反复穿透到数据库。
     * </p>
     */
    @Override
    public R<Product> getProductById(Long id) {
        String cacheKey = PRODUCT_CACHE_KEY + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        // 命中空值占位符 → 商品不存在
        if (NULL_CACHE_VALUE.equals(cached)) {
            throw new BusinessException("商品不存在");
        }

        // 命中正常缓存 → 直接返回
        if (cached instanceof Product) {
            return R.ok((Product) cached);
        }

        // 缓存未命中（cached == null，key 不存在或已过期）→ 查询数据库
        if (cached == null) {
            Product product = productMapper.selectById(id);
            if (product == null) {
                // 缓存空值，防止穿透（过期时间较短，避免长期占用）
                redisTemplate.opsForValue().set(cacheKey, NULL_CACHE_VALUE, CACHE_TTL, TimeUnit.MINUTES);
                throw new BusinessException("商品不存在");
            }

            // 回写缓存
            redisTemplate.opsForValue().set(cacheKey, product, CACHE_TTL, TimeUnit.MINUTES);
            return R.ok(product);
        }

        // 理论上不可达：cached 既不是 null、也不是 "NULL" 占位符、也不是 Product
        throw new BusinessException("缓存数据类型异常");
    }

    /**
     * 分页查询商品列表（不支持缓存，因查询条件多变）
     */
    @Override
    public R<PageResult<Product>> getProductPage(Integer page, Integer pageSize, String name, String status) {
        // status 为 null 时不加状态条件（查全部），用户端首页由前端显式传 status="ON" 只看上架商品
        long offset = (long) (page - 1) * pageSize;
        List<Product> records = productMapper.selectPage(offset, pageSize, name, status);
        Long total = productMapper.selectCount(name, status);
        return R.ok(PageResult.of(records, total, page, pageSize));
    }

    /**
     * 按分类分页查询上架商品
     */
    @Override
    public R<PageResult<Product>> getProductsByCategory(Long categoryId, Integer page, Integer pageSize) {
        long offset = (long) (page - 1) * pageSize;
        List<Product> records = productMapper.selectByCategory(categoryId, offset, pageSize);
        Long total = productMapper.selectCountByCategory(categoryId);
        return R.ok(PageResult.of(records, total, page, pageSize));
    }

    /**
     * 新增商品
     */
    @Override
    public R<Product> createProduct(Product product) {
        if (product.getStatus() == null || product.getStatus().isEmpty()) {
            product.setStatus("ON");
        }
        productMapper.insert(product);
        return R.ok("新增商品成功", product);
    }

    /**
     * 更新商品信息（更新后清除缓存）
     */
    @Override
    public R<Product> updateProduct(Product product) {
        Product exist = productMapper.selectById(product.getId());
        if (exist == null) {
            throw new BusinessException("商品不存在");
        }
        productMapper.update(product);
        // 清除旧缓存，下次查询时会自动回写最新数据
        redisTemplate.delete(PRODUCT_CACHE_KEY + product.getId());
        Product updated = productMapper.selectById(product.getId());
        return R.ok("更新商品成功", updated);
    }

    /**
     * 删除商品（删除后清除缓存）
     */
    @Override
    public R<Void> deleteProduct(Long id) {
        Product exist = productMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("商品不存在");
        }
        productMapper.deleteById(id);
        redisTemplate.delete(PRODUCT_CACHE_KEY + id);
        return R.ok("删除商品成功");
    }
}
