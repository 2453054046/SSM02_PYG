package com.pinyougou.user.service.impl;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.pojo.TbUserExample;
import com.pinyougou.pojo.TbUserExample.Criteria;
import com.pinyougou.user.service.UserService;

import entity.PageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;


/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class UserServiceImpl implements UserService {
    //用户表
	@Autowired
	private TbUserMapper userMapper;
    //redis缓存
	@Autowired
    private RedisTemplate redisTemplate;
	//消息队列
    @Autowired
    JmsTemplate jmsTemplate;
	@Autowired
    Destination smsDestination;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbUser> findAll() {
		return userMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbUser> page=   (Page<TbUser>) userMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbUser user) {
        //完善用户信息
        user.setCreated(new Date());//注册时间
        user.setUpdated(new Date());//修改时间
        user.setSourceType("1");//注册来源
        //DigestUtils.md5Hex密码加密
        user.setPassword(DigestUtils.md5Hex(user.getPassword()));

        int insert = userMapper.insert(user);
        System.out.println(insert);
    }

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbUser user){
		userMapper.updateByPrimaryKey(user);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbUser findOne(Long id){
		return userMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			userMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbUser user, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbUserExample example=new TbUserExample();
		Criteria criteria = example.createCriteria();
		
		if(user!=null){			
						if(user.getUsername()!=null && user.getUsername().length()>0){
				criteria.andUsernameLike("%"+user.getUsername()+"%");
			}
			if(user.getPassword()!=null && user.getPassword().length()>0){
				criteria.andPasswordLike("%"+user.getPassword()+"%");
			}
			if(user.getPhone()!=null && user.getPhone().length()>0){
				criteria.andPhoneLike("%"+user.getPhone()+"%");
			}
			if(user.getEmail()!=null && user.getEmail().length()>0){
				criteria.andEmailLike("%"+user.getEmail()+"%");
			}
			if(user.getSourceType()!=null && user.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+user.getSourceType()+"%");
			}
			if(user.getNickName()!=null && user.getNickName().length()>0){
				criteria.andNickNameLike("%"+user.getNickName()+"%");
			}
			if(user.getName()!=null && user.getName().length()>0){
				criteria.andNameLike("%"+user.getName()+"%");
			}
			if(user.getStatus()!=null && user.getStatus().length()>0){
				criteria.andStatusLike("%"+user.getStatus()+"%");
			}
			if(user.getHeadPic()!=null && user.getHeadPic().length()>0){
				criteria.andHeadPicLike("%"+user.getHeadPic()+"%");
			}
			if(user.getQq()!=null && user.getQq().length()>0){
				criteria.andQqLike("%"+user.getQq()+"%");
			}
			if(user.getIsMobileCheck()!=null && user.getIsMobileCheck().length()>0){
				criteria.andIsMobileCheckLike("%"+user.getIsMobileCheck()+"%");
			}
			if(user.getIsEmailCheck()!=null && user.getIsEmailCheck().length()>0){
				criteria.andIsEmailCheckLike("%"+user.getIsEmailCheck()+"%");
			}
			if(user.getSex()!=null && user.getSex().length()>0){
				criteria.andSexLike("%"+user.getSex()+"%");
			}
	
		}
		
		Page<TbUser> page= (Page<TbUser>)userMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}


	@Value("${template_code}")
    String template_code;
	@Value("${sign_name}")
    String sign_name;
    /**
     * 发送验证码
     * @param phone
     */
    @Override
    public void createSmsCode(String phone) {
        //生成一个6位随机数
        String smscode = (long)(Math.random()*1000000)+"";
        System.out.println("验证码是："+smscode);
        //存入redis
        redisTemplate.boundHashOps("smscode").put(phone,smscode);
        //发送验证码给消息队列
        jmsTemplate.send(smsDestination,((session)->{
            MapMessage message = session.createMapMessage();         //创建Map发送对象
            message.setString("mobile",phone);                    //填写阿里大于的信息和发送信息
            message.setString("template_code",template_code);  //发送模板
            message.setString("sign_name",sign_name);           //发送签名
            Map map = new HashMap<>();
            map.put("name",smscode);
            message.setString("param", JSON.toJSONString(map));

            return message;
        }));


    }

    /**
     * 验证验证码是否正确
     * @param phone     用户的手机号（在redis缓存中的key）
     * @param code      用户的输入验证码
     * @return
     */
    @Override
    public boolean checkSmsCode(String phone, String code) {
        String  smscode = (String) redisTemplate.boundHashOps("smscode").get(phone);
        if(smscode==null){
            return false;
        }
        if(smscode.equals(code)){
            return false;
        }
        return true;
    }


}
