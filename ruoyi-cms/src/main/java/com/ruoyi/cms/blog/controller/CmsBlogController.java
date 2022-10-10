package com.ruoyi.cms.blog.controller;

import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.cms.fileInfo.service.ISysFileInfoService;
import com.ruoyi.cms.tag.domain.CmsTag;
import com.ruoyi.cms.tag.service.ICmsTagService;
import com.ruoyi.cms.type.domain.CmsType;
import com.ruoyi.cms.type.service.ICmsTypeService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.SysPermissionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.cms.blog.domain.CmsBlog;
import com.ruoyi.cms.blog.service.ICmsBlogService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;


/**
 * 文章管理Controller
 * 
 * @author ning
 * @date 2022-01-01
 */
@RestController
@RequestMapping("/cms/blog")
public class CmsBlogController extends BaseController
{
    @Autowired
    private ICmsBlogService cmsBlogService;

    @Autowired
    private ICmsTypeService cmsTypeService;

    @Autowired
    private ICmsTagService cmsTagService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private ISysFileInfoService sysFileInfoService;

    /**
     * 分页查询，查询所有已发布，也就是状态为1的文章
     * 首页查询文章列表
     */
    @GetMapping("/cms/cmsList")
    public TableDataInfo cmsList(CmsBlog cmsBlog)
    {
        //分页，实际上是new一个分页对象，设置默认参数
        startPage();
        //状态为发布
        cmsBlog.setStatus("1");
        List<CmsBlog> list = cmsBlogService.selectCmsBlogList(cmsBlog);
        return getDataTable(list);
    }

    /**
     * 根据文章id获取详细文章
     * 后台，查询所有文章，并为文章设置tag和type等信息
     */
    @GetMapping(value = { "/cms/detail/", "/cms/detail/{id}" })
    public AjaxResult getInfoDetail(@PathVariable(value = "id", required = false) Long id)
    {
        AjaxResult ajax = AjaxResult.success();
        CmsType cmsType = new CmsType();
        CmsTag cmsTag = new CmsTag();
        ajax.put("types", cmsTypeService.selectCmsTypeList(cmsType));
        ajax.put("tags", cmsTagService.selectCmsTagList(cmsTag));

        //根据博客id查询博客，若无id，则查询所有
        if (StringUtils.isNotNull(id))
        {
            ajax.put(AjaxResult.DATA_TAG, cmsBlogService.selectCmsBlogById(id));
        }
        return ajax;
    }

    /**
     * 首页按分类查询文章列表
     * 根据分类id查询文章列表
     */
    @GetMapping("/cms/cmsListByType/{id}")
    public TableDataInfo cmsListByTypeId(@PathVariable(value = "id", required = false) Long id)
    {
        startPage();
        //根据分类id查找到当前分类下所有文章
        List<CmsBlog> list = cmsBlogService.selectCmsBlogListByTypeId(id);
        return getDataTable(list);
    }

    /**
     * 首页按标签查询文章列表
     * 分页，根据标签查询
     */
    @GetMapping("/cms/cmsListByTag/{id}")
    public TableDataInfo cmsListByTagId(@PathVariable(value = "id", required = false) Long id)
    {
        startPage();
        List<CmsBlog> list = cmsBlogService.selectCmsBlogListByTagId(id);
        return getDataTable(list);
    }

    /**
     * 首页查询推荐文章列表
     * 根据阅读数量降序查找，如果阅读创建时间升序排列
     */
    @GetMapping("/cms/cmsListRecommend")
    public TableDataInfo cmsListRecommend(CmsBlog cmsBlog)
    {
        startPage();
        //状态为发布
        cmsBlog.setStatus("1");
        List<CmsBlog> list = cmsBlogService.selectCmsBlogListRecommend(cmsBlog);
        return getDataTable(list);
    }

    /**
     * 首页增加阅读量
     * 没阅读一次阅读量+1
     */
    @GetMapping("/cms/addBlogViews/{id}")
    public AjaxResult addBlogViews(@PathVariable(value = "id", required = false) Long id)
    {
        CmsBlog cmsBlog = cmsBlogService.selectCmsBlogById(id);
        Long views = cmsBlog.getViews();
        views+=Long.parseLong("1");
        cmsBlog.setViews(views);
        cmsBlogService.updateCmsBlog(cmsBlog);
        return AjaxResult.success(id);
    }

    /**
     * 随笔页查询文章列表
     * 分页，查询已发布的随笔
     */
    @GetMapping("/cms/cmsEssayList")
    public TableDataInfo cmsEssayList(CmsBlog cmsBlog)
    {
        startPage();
        //状态为发布
        cmsBlog.setStatus("1");
        List<CmsBlog> list = cmsBlogService.selectCmsBlogList(cmsBlog);
        return getDataTable(list);
    }

    /**
     * 查询文章管理列表
     * 后台文章管理，需要有cms:blog:list权限
     */
    @PreAuthorize("@ss.hasPermi('cms:blog:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsBlog cmsBlog)
    {
        startPage();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(getLoginUser().getUser());
        if (!SecurityUtils.isAdmin(getUserId())&&!roles.contains("admin")&&!roles.contains("cms")){
            //TODO
            cmsBlog.setCreateBy(getUsername());
        }
        List<CmsBlog> list = cmsBlogService.selectCmsBlogList(cmsBlog);
        return getDataTable(list);
    }

    /**
     * 导出文章管理列表
     * 将文章内容，标题，分类等导出excel
     */
    @PreAuthorize("@ss.hasPermi('cms:blog:export')")
    @Log(title = "文章管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CmsBlog cmsBlog)
    {
        List<CmsBlog> list = cmsBlogService.selectCmsBlogList(cmsBlog);
        ExcelUtil<CmsBlog> util = new ExcelUtil<CmsBlog>(CmsBlog.class);
        util.exportExcel(response, list, "文章管理数据");
    }

    /**
     * 获取文章管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('cms:blog:query')")
    @GetMapping(value = { "/", "/{id}" })
    public AjaxResult getInfo(@PathVariable(value = "id", required = false) Long id)
    {
        AjaxResult ajax = AjaxResult.success();
        CmsType cmsType = new CmsType();
        CmsTag cmsTag = new CmsTag();
        ajax.put("types", cmsTypeService.selectCmsTypeList(cmsType));
        ajax.put("tags", cmsTagService.selectCmsTagList(cmsTag));
        if (StringUtils.isNotNull(id))
        {
            ajax.put(AjaxResult.DATA_TAG, cmsBlogService.selectCmsBlogById(id));
        }
        return ajax;
    }

    /**
     * 新增文章
     *
     */
    @PreAuthorize("@ss.hasPermi('cms:blog:add')")
    @Log(title = "文章管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CmsBlog cmsBlog)
    {
        cmsBlog.setCreateBy(getUsername());
        //同步新增 博客标签表 和 博客分类表 数据
        Long blogId = cmsBlogService.insertCmsBlog(cmsBlog);
        if (blogId==null){
            return AjaxResult.error();
        }
        return AjaxResult.success(blogId);
    }

    /**
     * 修改文章管理
     */
    @PreAuthorize("@ss.hasPermi('cms:blog:edit')")
    @Log(title = "文章管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CmsBlog cmsBlog)
    {
        cmsBlog.setUpdateBy(getUsername());
        //删除原首图

        //先根据博客id查询出具体博客
        CmsBlog oldBlog = cmsBlogService.selectCmsBlogById(cmsBlog.getId());
        //图片为null或者图片和原图片不一致
        if (cmsBlog.getBlogPic().isEmpty()||!cmsBlog.getBlogPic().equals(oldBlog.getBlogPic())){
            if(!oldBlog.getBlogPic().isEmpty()){
                String blogPic = oldBlog.getBlogPic();
                //TODO
                if (blogPic!=null&&!"".equals(blogPic)){
                    int newFileNameSeparatorIndex = blogPic.lastIndexOf("/");
                    String FileName = blogPic.substring(newFileNameSeparatorIndex + 1).toLowerCase();
                    sysFileInfoService.deleteSysFileInfoByFileObjectName(FileName);
                }
            }
        }
        return toAjax(cmsBlogService.updateCmsBlog(cmsBlog));
    }

    /**
     * 删除文章管理
     */
    @PreAuthorize("@ss.hasPermi('cms:blog:remove')")
    @Log(title = "文章管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        //删除原首图
        for (Long id : ids) {
            CmsBlog oldBlog = cmsBlogService.selectCmsBlogById(id);
            //先判断原博客是否已删除
            if(!oldBlog.getBlogPic().isEmpty()){
                String blogPic = oldBlog.getBlogPic();
                if (blogPic!=null&&!"".equals(blogPic)){
                    //TODO
                    int newFileNameSeparatorIndex = blogPic.lastIndexOf("/");
                    String FileName = blogPic.substring(newFileNameSeparatorIndex + 1).toLowerCase();
                    sysFileInfoService.deleteSysFileInfoByFileObjectName(FileName);
                }
            }
        }
        return toAjax(cmsBlogService.deleteCmsBlogByIds(ids));
    }

    /**
     * 取消按钮-删除首图
     */
    @PreAuthorize("@ss.hasPermi('cms:blog:edit')")
    @PostMapping("/cancel")
    public AjaxResult cancel(@RequestBody CmsBlog cmsBlog)
    {
        String blogPic = cmsBlog.getBlogPic();
        if (blogPic!=null&&!"".equals(blogPic)){
            Long blogId = cmsBlog.getId();
            if (blogId==null){
                //TODO
                int newFileNameSeparatorIndex = blogPic.lastIndexOf("/");
                String FileName = blogPic.substring(newFileNameSeparatorIndex + 1).toLowerCase();
                sysFileInfoService.deleteSysFileInfoByFileObjectName(FileName);
            }else {
                String Pic = cmsBlogService.selectCmsBlogById(blogId).getBlogPic();
                if (!blogPic.equals(Pic)){
                    int newFileNameSeparatorIndex = blogPic.lastIndexOf("/");
                    String FileName = blogPic.substring(newFileNameSeparatorIndex + 1).toLowerCase();
                    sysFileInfoService.deleteSysFileInfoByFileObjectName(FileName);
                }
            }
        }
        return toAjax(1);
    }
}
