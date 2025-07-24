package org.mtvs.backend.global.cache;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 세션 내 임시 캐시
 * 단일 요청/세션 내에서 중복 API 호출을 방지하기 위한 임시 캐시
 * TTL: 5분 (분석 작업 완료 후 자동 삭제)
 */
@Component
public class SessionCache {
    
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);
    
    public SessionCache() {
        // 5분마다 만료된 캐시 정리
        cleanupExecutor.scheduleAtFixedRate(this::cleanup, 5, 5, TimeUnit.MINUTES);
    }
    
    /**
     * 캐시에서 데이터 조회
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return (T) entry.getValue();
        }
        cache.remove(key); // 만료된 엔트리 제거
        return null;
    }
    
    /**
     * 캐시에 데이터 저장 (TTL: 5분)
     */
    public void put(String key, Object value) {
        long expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
        cache.put(key, new CacheEntry(value, expiryTime));
    }
    
    /**
     * 특정 키 삭제
     */
    public void evict(String key) {
        cache.remove(key);
    }
    
    /**
     * 모든 캐시 삭제
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * 캐시 크기 반환
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * 만료된 엔트리 정리
     */
    private void cleanup() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().getExpiryTime() < now);
    }
    
    /**
     * 캐시 엔트리 클래스
     */
    private static class CacheEntry {
        private final Object value;
        private final long expiryTime;
        
        public CacheEntry(Object value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }
        
        public Object getValue() {
            return value;
        }
        
        public long getExpiryTime() {
            return expiryTime;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}