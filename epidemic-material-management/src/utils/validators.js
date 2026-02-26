/**
 * 表单验证规则工具函数集合
 * 封装了常用的表单校验逻辑，如手机号、密码、邮箱等
 */

/**
 * 手机号验证
 * @param {Object} rule 验证规则对象
 * @param {string} value 待验证的值
 * @param {Function} callback 回调函数
 */
export const validatePhone = (rule, value, callback) => {
  const phoneReg = /^1[3-9]\d{9}$/
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!phoneReg.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

/**
 * 密码强度验证
 * 要求长度不少于6位
 * @param {Object} rule 验证规则对象
 * @param {string} value 待验证的值
 * @param {Function} callback 回调函数
 */
export const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else {
    callback()
  }
}

/**
 * 确认密码验证
 * 需配合 validatePassword 使用，校验两次输入是否一致
 * @param {string} password 第一次输入的密码
 * @returns {Function} 验证函数
 */
export const validateConfirmPassword = (password) => {
  return (rule, value, callback) => {
    if (!value) {
      callback(new Error('请再次输入密码'))
    } else if (value !== password) {
      callback(new Error('两次输入的密码不一致'))
    } else {
      callback()
    }
  }
}

/**
 * 正整数验证
 * 校验输入是否为大于0的整数
 * @param {Object} rule 验证规则对象
 * @param {string|number} value 待验证的值
 * @param {Function} callback 回调函数
 */
export const validatePositiveInteger = (rule, value, callback) => {
  const num = Number(value)
  if (!value) {
    callback(new Error('请输入数值'))
  } else if (!Number.isInteger(num) || num <= 0) {
    callback(new Error('请输入正整数'))
  } else {
    callback()
  }
}

/**
 * 邮箱格式验证
 * @param {Object} rule 验证规则对象
 * @param {string} value 待验证的值
 * @param {Function} callback 回调函数
 */
export const validateEmail = (rule, value, callback) => {
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!value) {
    callback(new Error('请输入邮箱'))
  } else if (!emailReg.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

/**
 * 身份证号验证
 * 支持15位和18位身份证号
 * @param {Object} rule 验证规则对象
 * @param {string} value 待验证的值
 * @param {Function} callback 回调函数
 */
export const validateIdCard = (rule, value, callback) => {
  const idCardReg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  if (!value) {
    callback(new Error('请输入身份证号'))
  } else if (!idCardReg.test(value)) {
    callback(new Error('请输入正确的身份证号'))
  } else {
    callback()
  }
}

/**
 * 通用必填验证生成器
 * @param {string} message 错误提示信息
 * @returns {Object} 验证规则对象
 */
export const validateRequired = (message = '请输入内容') => {
  return { required: true, message, trigger: 'blur' }
}
