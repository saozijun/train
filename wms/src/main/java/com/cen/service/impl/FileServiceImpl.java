package com.cen.service.impl;

import com.cen.entity.FileD;
import com.cen.mapper.FileMapper;
import com.cen.service.IFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文件上传的列表 服务实现类
 * </p>
 *
 * @author volcano
 * 
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, FileD> implements IFileService {

}
