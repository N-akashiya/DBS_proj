package com.dbs.tpc_benchmark.service;

import com.dbs.tpc_benchmark.mapper.TableMapper;
import com.dbs.tpc_benchmark.typings.dto.ClientInfoDTO;
import com.dbs.tpc_benchmark.typings.tableList.ClientInfo;
import com.dbs.tpc_benchmark.typings.vo.ClientInfoVO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService {
    @Autowired
    private TableMapper tableMapper;

    @Transactional
    public ClientInfoVO getClientInfo(ClientInfoDTO clientInfoDTO) {
        int offset = (clientInfoDTO.getCurrentPage() - 1) * clientInfoDTO.getPageSize();
        List<ClientInfo> clientInfoList = tableMapper.getClientInfoPage(clientInfoDTO.getNameKeyword(), clientInfoDTO.getNationKeyword(), clientInfoDTO.getPageSize(), offset);
        int total = tableMapper.countClientInfo(clientInfoDTO.getNameKeyword(), clientInfoDTO.getNationKeyword());
        return ClientInfoVO.builder()
                .clientInfoList(clientInfoList)
                .total(total)
                .currentPage(clientInfoDTO.getCurrentPage())
                .pageSize(clientInfoDTO.getPageSize())
                .build();
    }
}
