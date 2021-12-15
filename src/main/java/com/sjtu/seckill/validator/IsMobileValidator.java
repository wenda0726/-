package com.sjtu.seckill.validator;


import com.sjtu.seckill.utils.ValidatorUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

//实现对应的接口
//两个泛型，一个是实现的接口，一个是传入的数据类型
public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {

    private boolean required = false;
    @Override
    //初始化
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if(required){
            return ValidatorUtil.isMobile(s);
        }else{
            if (s == null){
                return true;
            }else{
                return ValidatorUtil.isMobile(s);

            }
        }
    }
}
