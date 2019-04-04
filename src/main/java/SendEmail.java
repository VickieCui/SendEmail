
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Properties;

public class SendEmail {

    private String host;//要跟自己的邮箱匹配，比如QQ邮箱smtp.qq.com;126邮箱smtp.126.com;
    private  String user;//发送邮件的邮箱名
    private String password;//密码
    private String to;//接收邮件的邮箱名
    private String res[];
    private String fileAttachment = "";
    private String preAttachment = "";
    private String preUrl ;
    public String del ;
    public String output;
    String shell ;
    String rm ;
    String html;

    public SendEmail(HashMap<String,String> settings){
        host = settings.get("host");
        user = settings.get("user");
        password = settings.get("password");
        to = settings.get("to");
        output = settings.get("output");
        shell = settings.get("shell");
        rm = settings.get("rm");
        del = settings.get("del");
        preUrl = settings.get("preUrl");
        html = settings.get("html");
        if(to.contains(",")){
            res = to.split(",");
        }
    }

    public static void main (String args[])
            throws Exception {

        IniRead read = new IniRead();
        SendEmail email = new SendEmail(read.getConfig());
        email.setAttachment();
        if (email.del.equals("true")) {
            if (email.runSync()) {
                if (email.isFail()) {
                    email.send();
                }
                email.del();
            }
        } else {
            email.runSync();
            if (email.isFail()) {
                email.send();
            }
        }
    }

    public void send() throws MessagingException, IOException {
        // Get system properties
        Properties props = System.getProperties();
        // Setup mail server
        props.put("mail.smtp.host", host);
        // Get session
        Session session =
                Session.getInstance(props, null);
        session.setDebug(false);
        // Define message
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(user));
        if(res != null) {
            InternetAddress[] sendTo = new InternetAddress[res.length];
            for (int i = 0; i < res.length; i++) {
                sendTo[i] = new InternetAddress(res[i]);
            }
            message.addRecipients(Message.RecipientType.TO, sendTo);
        } else{
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        }
        message.setSubject("主站APP UI自动化失败");//此处设置邮件标题
        // create the message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        //fill message
        String url = createUrl();
        messageBodyPart.setText("Hi ,\n主站APP自动化用例失败了呢>_<，内网的小伙伴可以点击链接查看\n\uD83D\uDC49 " + url + " \uD83D\uDC48\n 在家的小伙伴可以下载附件(附件暂无截图信息)查看～");//此处为邮件内容
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        // Part two is attachment
        messageBodyPart = new MimeBodyPart();
        setAttachment();
        //CompressUtil.compress(getPreAttachment(), getFileAttachment());
        DataSource source = new FileDataSource(html);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName("result.html");
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
        String[] cmd = new String[]{"sh", "-c", shell};
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            InputStream fis=p.getInputStream();
            InputStreamReader isr=new InputStreamReader(fis);
            BufferedReader br=new BufferedReader(isr);
            String line=null;
            System.out.println("*");
            while((line=br.readLine())!=null)
            {
                System.out.println("*"+line);
            }
            System.out.println("run shell");
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
            url = preUrl+ timeStamp +"/failReport.html";
            System.out.println("Local HostAddress: "+addr.getHostAddress());

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return url;
    }

    //获得最新的报告文件夹 & 报告时间戳
    public String[] getLastestDic(){
        File file = new File(output);
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
        System.out.println("附件文件"+path[0]);
        return path;
    }

    public boolean isFail(){
        if (!preAttachment.equals("")) {
            File dic = new File(preAttachment);
            File[] files = dic.listFiles();
            for (File file : files) {
                System.out.println(file.getName());
                if (file.getName().contains("failReport")) {
                    return true;
                }
            }
        }
        return false;
    }


    //附件的生成前 & 生成后的地址
    public void setAttachment(){
        String path = getLastestDic()[0];
        fileAttachment = path+".zip";
        System.out.println("附件地址："+fileAttachment);
        preAttachment = path;
    }

    public String getFileAttachment(){
        return fileAttachment;
    }

    public String getPreAttachment(){
        return preAttachment;
    }

    public Boolean del(){
        String[] cmd = new String[]{"sh", "-c", rm};
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("run rm");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}