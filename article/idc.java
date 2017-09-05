
/**
 *  分布式id生成
 */

public class IdCreater {
    private static LoggerHelper logger = new LoggerHelper(IdCreater.class);

    private static final long idepoch = 1288834974657L;//相对起点时间
    private static final long sequenceMask = 127L;//2的7次方-1 . 7位序列号
    private static long lastTimestamp = -1L;
    private long sequence = 0L;
    private final long machineId;



    public IdCreater(long machineId) {
        logger.info("++++++++++++++++++machineId:" + machineId + "++++++++++++++++++");
        if(machineId > 0L&&machineId<128L) {
            this.machineId = machineId;
        } else {
            throw new IllegalArgumentException("machineId shouble be between 1 and 127");
        }
    }



    private synchronized long nextId( long busid) {
        long timestamp = this.timeGen();

        if(timestamp < lastTimestamp) {
            try {
                //有人修改机器时间了
                throw new RuntimeException("Clock moved backwards.  Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
            } catch (Exception var10) {
                var10.printStackTrace();
            }
        }

        if(timestamp==lastTimestamp ) {
            //取模，一毫秒内超过128了阻塞下一毫秒
            this.sequence = this.sequence + 1L & sequenceMask;
            if(this.sequence == 0L) {
                timestamp = this.tilNextMillis(lastTimestamp);
            }
        } else {
            this.sequence = 0L;
        }

        lastTimestamp = timestamp;
        long nextId = (timestamp - idepoch) << 22 | machineId << 15 | busid << 7 | this.sequence;
        return nextId;
    }


    private long tilNextMillis(long lastTimestamp) {
        long timestamp;
        for(timestamp = this.timeGen(); timestamp <= lastTimestamp; timestamp = this.timeGen()) {
        }

        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}

