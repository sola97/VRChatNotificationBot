package cn.sola97.vrchat.aop.proxy;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JDAProxy {
    private static final Logger logger = LoggerFactory.getLogger(JDAProxy.class);
    private final JDABuilder jdaBuilder;
    private JDA jda;

    public JDAProxy(JDABuilder jdaBuilder) {
        this.jdaBuilder = jdaBuilder;
        buildJDA();
    }

    public JDA getJda() {
        return jda;
    }

    public void setJda(JDA jda) {
        this.jda = jda;
    }

    public Boolean isConnected() {
        if (jda == null) return false;
        return this.jda.getStatus().equals(JDA.Status.CONNECTED);
    }

    public JDA.Status getStatus() {
        if (jda != null)
            return this.jda.getStatus();
        return JDA.Status.SHUTDOWN;
    }

    public void shutdownNow() {
        if (jda != null) {
            this.jda.setAutoReconnect(false);
            this.jda.shutdownNow();
            this.jda = null;
        }
    }

    public void rebuild() {
        this.shutdownNow();
        logger.info("正在重新实例化JDA");
        buildJDA();
    }

    private void buildJDA() {
        try {
            this.jda = jdaBuilder.build();
        } catch (Exception e) {
            logger.error("build jda failed. error:" + e.getMessage());
        }
    }
}
