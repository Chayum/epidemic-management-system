package com.epidemic.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.material.entity.DonationCertificate;
import com.epidemic.material.mapper.DonationCertificateMapper;
import com.epidemic.material.service.DonationCertificateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 捐赠证书服务实现类
 */
@Service
@Slf4j
public class DonationCertificateServiceImpl extends ServiceImpl<DonationCertificateMapper, DonationCertificate> implements DonationCertificateService {

    @Override
    public DonationCertificate getByDonationId(String donationId) {
        return getOne(new LambdaQueryWrapper<DonationCertificate>()
                .eq(DonationCertificate::getDonationId, donationId));
    }

    @Override
    public DonationCertificate generateCertificate(String donationId, Long donorId, String donorName,
                                                    String materialType, Integer quantity, String unit) {
        // 检查是否已存在证书
        DonationCertificate existingCert = getByDonationId(donationId);
        if (existingCert != null) {
            log.info("捐赠证书已存在，donationId: {}, certificateNo: {}", donationId, existingCert.getCertificateNo());
            return existingCert;
        }

        // 生成证书编号：CERT + 年月日 + 6位序列号
        String certificateNo = generateCertificateNo();

        // 创建证书
        DonationCertificate certificate = new DonationCertificate();
        certificate.setDonationId(donationId);
        certificate.setDonorId(donorId);
        certificate.setDonorName(donorName);
        certificate.setCertificateNo(certificateNo);
        certificate.setMaterialType(materialType);
        certificate.setQuantity(quantity);
        certificate.setUnit(unit);
        certificate.setIssueTime(LocalDateTime.now());
        certificate.setCreateTime(LocalDateTime.now());

        save(certificate);
        log.info("捐赠证书生成成功，certificateNo: {}, donorName: {}", certificateNo, donorName);

        return certificate;
    }

    /**
     * 生成证书编号
     * 格式：CERT + 年月日时分秒 + 4位随机数
     */
    private String generateCertificateNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 9000) + 1000;
        return "CERT" + dateStr + random;
    }
}
