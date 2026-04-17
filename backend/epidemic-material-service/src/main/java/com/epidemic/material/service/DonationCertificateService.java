package com.epidemic.material.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epidemic.material.entity.DonationCertificate;

/**
 * 捐赠证书服务接口
 */
public interface DonationCertificateService extends IService<DonationCertificate> {

    /**
     * 根据捐赠ID获取证书
     * @param donationId 捐赠ID
     * @return 证书信息
     */
    DonationCertificate getByDonationId(String donationId);

    /**
     * 生成捐赠证书
     * @param donationId 捐赠ID
     * @param donorId 捐赠人ID
     * @param donorName 捐赠人姓名
     * @param materialType 物资类型
     * @param quantity 数量
     * @param unit 单位
     * @return 生成的证书
     */
    DonationCertificate generateCertificate(String donationId, Long donorId, String donorName,
                                            String materialType, Integer quantity, String unit);
}
