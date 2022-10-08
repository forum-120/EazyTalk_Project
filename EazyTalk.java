
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

public class EazyTalk extends JFrame {
    private JButton sendButton;     //发送按钮
    private JButton clearButton;      //清除聊天记录按钮
    private static int DEFAULT_PORT=8888;    //设置的初始端口号
    private JTextField ipTextField;   //输入ip地址的文本框
    private JTextField portTextField;   //输入端口号的文本框
    private JTextArea inputTextArea;    //输入要说的话的地方
    private JTextArea centerTextArea;   //显示聊天记录的区域
    private JLabel stateLB;             //设置端口状态信息
    private DatagramSocket datagramSocket;

    public void setUpUI(){
        //设置窗口的基本属性
        setTitle("EazyTalk聊天");
        setLayout(new BorderLayout());
        setBounds(400,300,700,600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        //设置窗口上方的提示信息
        stateLB=new JLabel();
        stateLB.setText("窗口还未开始监听");
        stateLB.setHorizontalAlignment(JLabel.RIGHT);
        add(stateLB,BorderLayout.NORTH);

        //设置显示聊天记录的部分
        centerTextArea=new JTextArea();
        centerTextArea.setEditable(false);        //不可以对聊天记录进行修改
        centerTextArea.setBackground(Color.cyan);
        add(new JScrollPane(centerTextArea),BorderLayout.CENTER);

        //设置窗口下方的输入框和ip地址和端口输入框
        JPanel southPanel=new JPanel();
        southPanel.setLayout(new BorderLayout());
        inputTextArea=new JTextArea(5,30);
        southPanel.add(inputTextArea,BorderLayout.NORTH);

        JPanel bottomPanel=new JPanel();
        ipTextField=new JTextField("127.0.0.1",10);
        portTextField=new JTextField(String.valueOf(DEFAULT_PORT),3);
        sendButton=new JButton("发送");
        clearButton=new JButton("清空");
        bottomPanel.add(ipTextField);
        bottomPanel.add(portTextField);
        bottomPanel.add(sendButton);
        bottomPanel.add(clearButton);
        southPanel.add(bottomPanel,BorderLayout.SOUTH);
        add(southPanel,BorderLayout.SOUTH);

        //使窗口可视化
        setResizable(false);
    }
    //设置各个监听事件
    public void setListener(){
        sendButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                String ipAddress=ipTextField.getText();
                String remotePort=portTextField.getText();
                if(ipAddress==null||ipAddress.trim().equals("")
                        ||remotePort==null||remotePort.trim().equals("")){
                    JOptionPane.showMessageDialog(EazyTalk.this,"请输入IP地址和端口号");
                }
                if(datagramSocket==null||datagramSocket.isClosed()){
                    JOptionPane.showMessageDialog(EazyTalk.this,"监听端口出错");
                    return;
                }
                String sendContent=inputTextArea.getText();
                byte[] buf=sendContent.getBytes();
                try{
                    centerTextArea.append("我对"+ipAddress+"::"+remotePort+"说:\n"+inputTextArea.getText()+"\n\n");
                    centerTextArea.setCaretPosition(centerTextArea.getText().length());
                    datagramSocket.send(new DatagramPacket(buf,buf.length,
                            InetAddress.getByName(ipAddress),Integer.parseInt(remotePort)));
                    inputTextArea.setText("");
                }catch(Exception e1){
                    JOptionPane.showMessageDialog(EazyTalk.this,"无法发送");
                    System.out.println(e1.getMessage());
                }
            }
        });
        clearButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                centerTextArea.setText("");
            }
        });
    }
    public void initSocket(){
        int port=DEFAULT_PORT;
        while(true){
            try{
                if(datagramSocket!=null&&!datagramSocket.isClosed()){
                    datagramSocket.close();
                }
                try{
                    port=Integer.parseInt(JOptionPane.showInputDialog(this,"请输入端口号"
                            ,"端口号",JOptionPane.QUESTION_MESSAGE));
                }catch (Exception e){
                    JOptionPane.showMessageDialog(null,"端口号范围得在1——65535");
                    continue;
                }
                datagramSocket=new DatagramSocket(port);
                startListen();
                stateLB.setText("在"+port+"端口监听");
                break;
            }catch(Exception e){
                JOptionPane.showMessageDialog(this,"端口被占用，请重新设置端口");
                stateLB.setText("当前无启用端口");
            }
        }
    }
    private void startListen(){
        new Thread(){
            private DatagramPacket p;
            public void run(){
                byte[] buf=new byte[1024];
                p=new DatagramPacket(buf,buf.length);
                while(!datagramSocket.isClosed()){
                    try{
                        datagramSocket.receive(p);
                        centerTextArea.append(p.getAddress().getHostAddress()+"::"+
                                ((InetSocketAddress)p.getSocketAddress()).getPort()+"对我说:\n"+
                                new String(p.getData(),0,p.getLength())+"\n\n");
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public EazyTalk(){
        setUpUI();
        setListener();
        initSocket();
    }

    public static void main(String[] args){
        EazyTalk EazyTalk=new EazyTalk();
    }
}