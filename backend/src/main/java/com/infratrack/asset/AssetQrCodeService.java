package com.infratrack.asset;

import com.infratrack.exception.NotFoundException;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates asset QR code images for authorized users (V2.4.0 Sprint M4-BE2).
 * Encodes the stable asset business code only — never database ids or URLs.
 */
@Service
public class AssetQrCodeService {

    private final UserService userService;
    private final AssetRepository assetRepository;
    private final AssetAuthorizationService assetAuthorizationService;
    private final QrCodeGenerator qrCodeGenerator;

    public AssetQrCodeService(
            UserService userService,
            AssetRepository assetRepository,
            AssetAuthorizationService assetAuthorizationService,
            QrCodeGenerator qrCodeGenerator) {
        this.userService = userService;
        this.assetRepository = assetRepository;
        this.assetAuthorizationService = assetAuthorizationService;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    @Transactional(readOnly = true)
    public byte[] generateQrCodePng(Long userId, Long assetId) {
        User user = userService.getById(userId);
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new NotFoundException("Asset not found"));
        assetAuthorizationService.requireCanViewAsset(user, asset);
        return qrCodeGenerator.generatePng(asset.getCode());
    }
}
