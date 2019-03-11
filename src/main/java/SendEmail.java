
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.util.Properties;

public class SendEmail {

    private String host = "smtp.163.com";//要跟自己的邮箱匹配，比如QQ邮箱smtp.qq.com;126邮箱smtp.126.com;
    private String user = "qa_vision@163.com";//发送邮件的邮箱名
    private String password = "123baixing";//密码
    private String to[] = {"cuimingyue@baixing.com","guizhanluo@baixing.com"};//接收邮件的邮箱名
    private String fileAttachment = "";
    private String fileName = "";
    boolean isFail = false;
    public String output = "/home/ubuntu/.jenkins/workspace/SeleniumPython/report";
    //public String output = "/Users/cuimingyue/Desktop/SeleniumPython/report";

    public static void main(String args[])
            throws Exception {
        String from = "qa_vision@163.com";//发送邮件的邮箱名
        SendEmail email = new SendEmail();
        email.setAttachment();
        if (email.isFail) {
            email.send(from);
        }
    }

    public void send(String from) throws MessagingException, IOException {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", host);
        Session session =
                Session.getInstance(props, null);
        session.setDebug(false);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        InternetAddress[] sendTo = new InternetAddress[to.length];
        for (int i = 0; i < to.length; i++) {
            sendTo[i] = new InternetAddress(to[i]);
        }
        message.addRecipients(Message.RecipientType.TO, sendTo);
        message.setSubject("SeleniumPython UI automation failed");//此处设置邮件标题
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent("Selenium Python automation has been failed ,for details please see the attachment. \n Or click the url http://vision.baixing.cn/jenkins/job/SeleniumPython/HTML_20Report/","text/html; charset=utf-8");
       // messageBodyPart.setText("Hi ,Selenium Python UI automation has failed. For more details,see the attachment.");//此处为邮件内容
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        messageBodyPart = new MimeBodyPart();
        setAttachment();
        System.out.println("file Name" + fileAttachment);
        DataSource source = new FileDataSource(fileAttachment);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(fileName);
        multipart.addBodyPart(messageBodyPart);
        message.setContent(multipart);
        Transport transport = session.getTransport("smtp");
        transport.connect(host, user, password);
        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
        System.out.println("ok");
        transport.close();
    }

    //获得最新的报告文件夹 & ped

    public String[] getLastestDic() {
        File file = new File(output);
        File[] reports = file.listFiles();
        long createTime = 0;
        String path[] = new String[2];
        for (File report : reports) {
            String name = report.getName().replace(" ", "");
            if (name.contains("result") && name.contains("html")) {
                if (report.lastModified() > createTime) {
                    createTime = report.lastModified();
                    path[0] = report.getAbsolutePath();
                    path[1] = report.getName();
                    System.out.println(path[0]);
                }
            }
            if (path[1].contains("fail")) {
                isFail = true;
            }
        }
        return path;
    }

    public String getHtml() {
        File file = new File(fileAttachment);
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                System.out.println(tempString);
                sb.append(tempString.toString());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return sb.toString();
    }

    //附件信息
    public void setAttachment() {
        String path[] = getLastestDic();
        fileAttachment = path[0];
        System.out.println(fileAttachment);
        fileName = path[1];
    }

    public String getFileAttachment() {
        return fileAttachment;
    }

    public String getFileName() {
        return fileName;
    }

}