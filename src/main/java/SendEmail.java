
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class SendEmail {

    private String host = "smtp.163.com";//要跟自己的邮箱匹配，比如QQ邮箱smtp.qq.com;126邮箱smtp.126.com;
    private  String user="qa_vision@163.com";//发送邮件的邮箱名
    private String password="123baixing";//密码
    private String shPath = "/Users/cuimingyue/Desktop/sync.sh";
    private String to = "cuimingyue@baixing.com";//接收邮件的邮箱名
    private String fileAttachment = "";
    private String preAttachment = "";

    public static void main (String args[])
            throws Exception {
        String from = "qa_vision@163.com";//发送邮件的邮箱名
        SendEmail email = new SendEmail();
        email.setAttachment();
        if(email.runSync()) {
            if(email.isFail()) {
                email.send(from);
            }
        }
    }

    public void send(String from) throws MessagingException, IOException {
        // Get system properties
        Properties props = System.getProperties();
        // Setup mail server
        props.put("mail.smtp.host", host);
        // Get session
        Session session =
                Session.getInstance(props, null);
        session.setDebug(true);
        // Define message
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject("主站APP UI自动化失败");//此处设置邮件标题
        // create the message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        //fill message
        String url = createUrl();
        messageBodyPart.setText("Hi ,\n主站APP自动化用例失败了呢>_<，内网的小伙伴可以点击链接查看\n\uD83D\uDC49 " + url + " \uD83D\uDC48\n 在家的小伙伴可以下载附件查看～");//此处为邮件内容
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        // Part two is attachment
        messageBodyPart = new MimeBodyPart();
        setAttachment();
        CompressUtil.compress(getPreAttachment(), getFileAttachment());
        DataSource source = new FileDataSource(fileAttachment);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName("report.zip");
        multipart.addBodyPart(messageBodyPart);
        // Put parts in message
        message.setContent(multipart);
        // Send the message
        Transport transport = session.getTransport("smtp");
        transport.connect(host, user, password);
        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
        //transport.send(message);
        System.out.println("ok");
        transport.close();
    }

    public boolean runSync(){
        String shellString = "sh " + shPath;
        String[] cmd = new String[]{"sh", "-c", shellString};
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //获取云端url
    public String createUrl(){
        String url = "";
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
            String timeStamp = getLastestDic()[1];
            //url = "http://" + addr.getHostAddress() + ":8080/jiguanshu/output/"+ timeStamp +"/report.html";
            url = "http://vision.baixing.cn/jiguanshu/output/"+ timeStamp +"/failReport.html";
            System.out.println("Local HostAddress: "+addr.getHostAddress());

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return url;
    }

    //获得最新的报告文件夹 & 报告时间戳
    public String[] getLastestDic(){
        File file = new File("/Users/cuimingyue/Library/tomcat9/webapps/jiguanshu/output/");
        File[] reports = file.listFiles();
        long createTime = 0;
        String path[] = new String[2];
        for(File report:reports){
            if(report.isDirectory() && !report.getName().contains("screen") && !report.getName().contains(".")){
                if (Long.valueOf(report.getName()) > createTime){
                    createTime = Long.valueOf(report.getName());
                    path[0] = report.getAbsolutePath();
                }
            }
        }
        path[1] = String.valueOf(createTime);
        return path;
    }

    public boolean isFail(){
        File dic = new File(preAttachment);
        File[] files = dic.listFiles();
        for(File file:files){
            System.out.println(file.getName());
            if(file.getName().contains("failReport")){
                return true;
            }
        }
        return false;
    }


    //附件的生成前 & 生成后的地址
    public void setAttachment(){
        String path = getLastestDic()[0];
        fileAttachment = path+".zip";
        preAttachment = path;
    }

    public String getFileAttachment(){
        return fileAttachment;
    }

    public String getPreAttachment(){
        return preAttachment;
    }


}