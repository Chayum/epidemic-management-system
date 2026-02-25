package com.epidemic.pandemic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.common.result.PageResult;
import com.epidemic.pandemic.entity.PandemicNews;
import com.epidemic.pandemic.mapper.PandemicNewsMapper;
import com.epidemic.pandemic.service.PandemicService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 疫情信息服务实现类
 */
@Service
public class PandemicServiceImpl extends ServiceImpl<PandemicNewsMapper, PandemicNews> implements PandemicService {

    @Override
    public PageResult<PandemicNews> getNewsList(Integer page, Integer size, String status) {
        Page<PandemicNews> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PandemicNews> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(PandemicNews::getStatus, status);
        }
        wrapper.orderByDesc(PandemicNews::getPublishTime);
        
        Page<PandemicNews> result = baseMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public void publishNews(PandemicNews news) {
        news.setId("N" + System.currentTimeMillis());
        news.setCreateTime(LocalDateTime.now());
        if (news.getPublishTime() == null) {
            news.setPublishTime(LocalDateTime.now());
        }
        if (news.getViewCount() == null) {
            news.setViewCount(0);
        }
        if (!StringUtils.hasText(news.getAuthor())) {
            news.setAuthor("系统管理员");
        }
        baseMapper.insert(news);
    }

    @Override
    public PageResult<Map<String, Object>> getKnowledgeList(Integer page, Integer size) {
        // Mock implementation for Knowledge as it's not fully defined in DB schema
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> knowledge1 = new HashMap<>();
        knowledge1.put("id", "K001");
        knowledge1.put("title", "如何正确佩戴N95口罩");
        knowledge1.put("summary", "正确佩戴N95口罩是预防呼吸道传染病的重要措施，请按照以下步骤操作。");
        knowledge1.put("category", "personal");
        knowledge1.put("author", "疾控中心");
        knowledge1.put("viewCount", 3250);
        knowledge1.put("sortOrder", 1);
        list.add(knowledge1);

        Map<String, Object> knowledge2 = new HashMap<>();
        knowledge2.put("id", "K002");
        knowledge2.put("title", "家庭日常消毒指南");
        knowledge2.put("summary", "家庭日常消毒是预防疫情的重要环节，请了解以下消毒方法和注意事项。");
        knowledge2.put("category", "community");
        knowledge2.put("author", "疾控中心");
        knowledge2.put("viewCount", 2180);
        knowledge2.put("sortOrder", 2);
        list.add(knowledge2);

        Map<String, Object> knowledge3 = new HashMap<>();
        knowledge3.put("id", "K003");
        knowledge3.put("title", "医疗机构内个人防护要点");
        knowledge3.put("summary", "医疗机构内工作人员应严格做好个人防护，防止职业暴露。");
        knowledge3.put("category", "hospital");
        knowledge3.put("author", "卫健委");
        knowledge3.put("viewCount", 1680);
        knowledge3.put("sortOrder", 3);
        list.add(knowledge3);

        return PageResult.of(list, 10L, page, size);
    }

    @Override
    public Map<String, Object> getPandemicData() {
        Map<String, Object> data = new HashMap<>();
        data.put("confirmed", 156);
        data.put("cured", 120);
        data.put("dead", 3);
        data.put("suspected", 10);
        data.put("updateTime", LocalDateTime.now());
        return data;
    }

    @Override
    public List<Map<String, Object>> getPushStats() {
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> stat1 = new HashMap<>();
        stat1.put("label", "推送总数");
        stat1.put("value", "1,256");
        list.add(stat1);
        
        Map<String, Object> stat2 = new HashMap<>();
        stat2.put("label", "今日推送");
        stat2.put("value", "28");
        list.add(stat2);
        
        Map<String, Object> stat3 = new HashMap<>();
        stat3.put("label", "推送成功");
        stat3.put("value", "1,234");
        list.add(stat3);
        
        Map<String, Object> stat4 = new HashMap<>();
        stat4.put("label", "推送失败");
        stat4.put("value", "22");
        list.add(stat4);
        
        return list;
    }

    @Override
    public List<Map<String, Object>> getPushList() {
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> record1 = new HashMap<>();
        record1.put("title", "物资紧缺提醒");
        record1.put("target", "医院用户");
        record1.put("channelList", new String[]{"APP", "SMS"});
        record1.put("time", "2026-02-24 10:30:00");
        record1.put("status", "成功");
        list.add(record1);
        
        Map<String, Object> record2 = new HashMap<>();
        record2.put("title", "新政策发布");
        record2.put("target", "全部用户");
        record2.put("channelList", new String[]{"APP", "WEB"});
        record2.put("time", "2026-02-24 09:00:00");
        record2.put("status", "成功");
        list.add(record2);
        
        return list;
    }

    @Override
    public void sendPush(Map<String, Object> pushData) {
        // Mock send push
        System.out.println("Sending push: " + pushData);
    }
}
