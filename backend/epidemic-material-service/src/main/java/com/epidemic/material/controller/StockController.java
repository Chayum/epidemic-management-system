package com.epidemic.material.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.common.util.UserContext;
import com.epidemic.common.annotation.OperateLog;
import com.epidemic.material.dto.StockOrderDTO;
import com.epidemic.material.entity.InventoryLog;
import com.epidemic.material.entity.StockOrder;
import com.epidemic.material.service.InventoryLogService;
import com.epidemic.material.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import com.epidemic.material.vo.InventoryLedgerVO;

/**
 * 库存管理控制器
 */
@Tag(name = "库存管理", description = "出入库单据、库存变动日志")
@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final InventoryLogService inventoryLogService;

    @Operation(summary = "查询库存台账")
    @GetMapping("/ledger")
    public Result<PageResult<InventoryLedgerVO>> getInventoryLedger(@RequestParam(defaultValue = "1") Integer page,
                                                                    @RequestParam(defaultValue = "10") Integer size,
                                                                    @RequestParam(required = false) String keyword) {
        Page<InventoryLedgerVO> result = stockService.getInventoryLedger(page, size, keyword);
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), page, size));
    }

    @Operation(summary = "创建出入库单")
    @PostMapping("/order")
    @OperateLog(module = "出入库", operation = "创建单据")
    public Result<String> createOrder(@Valid @RequestBody StockOrderDTO dto) {
        String orderId = stockService.createOrder(dto, UserContext.getUserId(), UserContext.getUsername());
        return Result.success(orderId);
    }

    @Operation(summary = "审核单据")
    @PostMapping("/order/{id}/audit")
    @OperateLog(module = "出入库", operation = "审核单据")
    public Result<String> auditOrder(@PathVariable String id,
                                     @RequestParam boolean approved,
                                     @RequestParam(required = false) String remark) {
        stockService.auditOrder(id, UserContext.getUserId(), UserContext.getUsername(), approved, remark);
        return Result.success("审核完成");
    }

    @Operation(summary = "作废单据")
    @PostMapping("/order/{id}/void")
    @OperateLog(module = "出入库", operation = "作废单据")
    public Result<String> voidOrder(@PathVariable String id) {
        stockService.voidOrder(id, UserContext.getUserId());
        return Result.success("作废成功");
    }

    @Operation(summary = "查询单据列表")
    @GetMapping("/order/list")
    public Result<PageResult<StockOrder>> getOrderList(@RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer size,
                                                       @RequestParam(required = false) String type,
                                                       @RequestParam(required = false) String status,
                                                       @RequestParam(required = false) String keyword) {
        Page<StockOrder> result = stockService.getOrderList(page, size, type, status, keyword);
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), page, size));
    }

    @Operation(summary = "查询库存变动日志")
    @GetMapping("/log/list")
    public Result<PageResult<InventoryLog>> getLogList(@RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer size,
                                                       @RequestParam(required = false) String materialId,
                                                       @RequestParam(required = false) String type,
                                                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Page<InventoryLog> result = inventoryLogService.getLogList(page, size, materialId, type, startTime, endTime);
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), page, size));
    }
}
