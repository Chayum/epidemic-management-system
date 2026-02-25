package com.epidemic.material.convert;

import com.epidemic.material.dto.ApplicationSubmitDTO;
import com.epidemic.material.entity.Application;
import com.epidemic.material.vo.ApplicationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物资申领转换器
 */
@Component
public class ApplicationConverter {

    /**
     * DTO转Entity
     */
    public Application toEntity(ApplicationSubmitDTO dto) {
        if (dto == null) {
            return null;
        }
        Application entity = new Application();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    /**
     * Entity转VO
     */
    public ApplicationVO toVO(Application entity) {
        if (entity == null) {
            return null;
        }
        ApplicationVO vo = new ApplicationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * EntityList转VOList
     */
    public List<ApplicationVO> toVOList(List<Application> entityList) {
        if (entityList == null) {
            return new ArrayList<>();
        }
        return entityList.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
}
