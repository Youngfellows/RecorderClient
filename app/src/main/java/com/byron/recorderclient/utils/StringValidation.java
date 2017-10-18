package com.byron.recorderclient.utils;

/**
 * Created by Jie.Chen on 2017/10/17.
 * 输入检查
 * 正则检查String是否符合某些格式
 */
public class StringValidation {
    final StringValidation self = this;

    public final static String RegexAllChinese = "^[\\u4e00-\\u9fa5]*$";
    public final static String RegexChineseName = "^[\\u4e00-\\u9fa5]{2,15}$"; // According To Statistics, the longest name has 15 character
    public final static String RegexPhoneNumber = "^(((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8})|((\\d{3,4}-)?\\d{7,8}(-\\d{1,4})?)$";
    public final static String RegexEmail = "w+([-+.]w+)*@w+([-.]w+)*.w+([-.]w+)*";
    public final static String RegexIP = "^(25[0-5]|2[0-4][0-9]|1{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|1{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|1{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|1{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";
    public final static String RegexPort = "^6553[0-5]|655[0-2][0-9]|65[0-4][0-9]{2}|6[0-4][0-9]{3}|[1-5][0-9]{4}|[1-9][0-9]{0,3}$";


    public static boolean validateRegularCharacter(String string, int minSize, int maxSize) {
        return validateRegex(string, "^\\w{" + minSize + "," + maxSize + "}$");
    }

    public static boolean validateRegex(String string, String regex) {
        if (string == null) {
            return false;
        }

        return string.matches(regex);
    }

    /**
     * 检查ip和端口是否合法
     *
     * @param ipAddress
     * @param port
     * @return
     */
    public static boolean checkValidation(String ipAddress, String port) {
        boolean ipValidation = false;
        boolean portValidation = false;
        if (StringValidation.validateRegex(ipAddress, StringValidation.RegexIP)) {
            ipValidation = true;
        }
        if (StringValidation.validateRegex(port, StringValidation.RegexPort)) {
            portValidation = true;
        }
        return ipValidation && portValidation;
    }
}