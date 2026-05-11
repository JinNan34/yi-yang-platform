/** 中国大陆手机号（宽松：1 开头 11 位） */
export const MOBILE_PATTERN = /^1[3-9]\d{9}$/

/** 18 位身份证（末位可为 X），实训场景仅做形态校验 */
export const ID_CARD_18 = /^\d{17}[\dXx]$/

export function required(msg, trigger = 'blur') {
  return [{ required: true, message: msg, trigger }]
}

export function optionalMobile(_rule, value, callback) {
  const v = (value == null ? '' : String(value)).trim()
  if (!v) {
    callback()
    return
  }
  if (!MOBILE_PATTERN.test(v)) {
    callback(new Error('请输入 11 位手机号'))
    return
  }
  callback()
}

export function idCard18(_rule, value, callback) {
  const v = (value == null ? '' : String(value)).trim().toUpperCase()
  if (!v) {
    callback(new Error('请填写身份证号'))
    return
  }
  if (v.length !== 18) {
    callback(new Error('身份证号应为 18 位'))
    return
  }
  if (!ID_CARD_18.test(v)) {
    callback(new Error('身份证号格式不正确'))
    return
  }
  callback()
}

/** 收缩压应大于等于舒张压（两字段均有值时校验） */
export function bloodPressurePair(form) {
  return (_rule, _value, callback) => {
    const s = form.systolicBp
    const d = form.diastolicBp
    if (s != null && d != null && Number(s) < Number(d)) {
      callback(new Error('收缩压应大于或等于舒张压'))
      return
    }
    callback()
  }
}
