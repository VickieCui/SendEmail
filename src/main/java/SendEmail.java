
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

import java.net.InetAddress;
import java.util.Date;
import java.util.Properties;


public class SendEmail {
    private String host = "smtp.163.com";//要跟自己的邮箱匹配，比如QQ邮箱smtp.qq.com;126邮箱smtp.126.com;
    private  String user="qa_vision@163.com";//发送邮件的邮箱名
    private String password="123baixing";//密码
    private String from = "qa_vision@163.com";//发送邮件的邮箱名
    private String to = "cuimingyue@baixing.com";//接收邮件的邮箱名
    private String fileAttachment = "/Users/cuimingyue/Library/tomcat9/webapps/jiguanshu/output/report.html";//附件地址

    public static void main (String args[])
            throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        System.out.println("Local HostAddress: "+addr.getHostAddress());
        String hostname = addr.getHostName();
        System.out.println("Local host name: "+hostname);

    }

    public void send(String from ,String title, String content) throws MessagingException {
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
        messageBodyPart.setText("主站APP自动化用例失败了呢>_<，请下载附件查看报告");//此处为邮件内容
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        // Part two is attachment
        messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(fileAttachment);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName("report.html");
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
}