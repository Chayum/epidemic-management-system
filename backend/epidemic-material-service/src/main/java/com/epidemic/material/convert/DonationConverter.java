package com.epidemic.material.convert;

import com.epidemic.material.dto.DonationSubmitDTO;
import com.epidemic.material.entity.Donation;
import com.epidemic.material.vo.DonationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 捐赠信息转换器
 */
@Component
public class DonationConverter {

    public Donation toEntity(DonationSubmitDTO dto) {
        if (dto == null) {
            return null;
        }
        Donation entity = new Donation();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    public DonationVO toVO(Donation entity) {
        if (entity == null) {
            return null;
        }
        DonationVO vo = new DonationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public List<DonationVO> toVOList(List<Donation> entityList) {
        if (entityList == null) {
            return new ArrayList<>();
        }
        return entityList.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
}
