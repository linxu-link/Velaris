/*
 * Copyright 2026 WuJia(Linxu_Link)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wujia.foundation.ads

import android.app.Activity
import androidx.test.core.app.ApplicationProvider
import com.wujia.foundation.ads.rewarded.RewardedAdClock
import com.wujia.foundation.ads.rewarded.SharedPreferencesRewardedAdUsageStore
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
class RewardedAdGatekeeperTest {
    private val dispatcher = StandardTestDispatcher()
    private val activity = Activity()

    @Test
    fun showIfRequired_skipsAdWithinHourlyCooldown() = runTest(dispatcher) {
        val clock = FakeRewardedAdClock(nowMillis = 2_000L)
        val store = FakeRewardedAdUsageStore(
            usage = RewardedAdUsage(
                localDate = "today",
                dailyRewardCount = 1,
                lastRewardEarnedAtMillis = 1_000L,
            ),
        )
        val manager = FakeRewardedAdManager(RewardedAdResult.RewardEarned)
        val gatekeeper = gatekeeper(clock = clock, usageStore = store, rewardedAdManager = manager)

        val result = gatekeeper.showIfRequired(activity, RewardedAdPlacement.NEW_SCENE_SAVE)

        assertEquals(RewardedAdResult.SkippedRecentReward, result)
        assertEquals(0, manager.showCalls)
        assertEquals(0, store.recordCalls)
    }

    @Test
    fun showIfRequired_skipsAdAfterDailyLimitReached() = runTest(dispatcher) {
        val store = FakeRewardedAdUsageStore(
            usage = RewardedAdUsage(
                localDate = "today",
                dailyRewardCount = 10,
                lastRewardEarnedAtMillis = 0L,
            ),
        )
        val manager = FakeRewardedAdManager(RewardedAdResult.RewardEarned)
        val gatekeeper = gatekeeper(usageStore = store, rewardedAdManager = manager)

        val result = gatekeeper.showIfRequired(activity, RewardedAdPlacement.NEW_SCENE_SAVE)

        assertEquals(RewardedAdResult.SkippedDailyLimitReached, result)
        assertEquals(0, manager.showCalls)
        assertEquals(0, store.recordCalls)
    }

    @Test
    fun showIfRequired_recordsRewardEarned() = runTest(dispatcher) {
        val clock = FakeRewardedAdClock(nowMillis = 20_000L)
        val store = FakeRewardedAdUsageStore(
            usage = RewardedAdUsage(
                localDate = "today",
                dailyRewardCount = 3,
                lastRewardEarnedAtMillis = 0L,
            ),
        )
        val manager = FakeRewardedAdManager(RewardedAdResult.RewardEarned)
        val gatekeeper = gatekeeper(clock = clock, usageStore = store, rewardedAdManager = manager)

        val result = gatekeeper.showIfRequired(activity, RewardedAdPlacement.NEW_SCENE_SAVE)

        assertEquals(RewardedAdResult.RewardEarned, result)
        assertEquals(1, manager.showCalls)
        assertEquals(1, store.recordCalls)
        assertEquals(20_000L, store.lastRecordedNowMillis)
    }

    @Test
    fun showIfRequired_technicalFailureDoesNotRecordReward() = runTest(dispatcher) {
        val store = FakeRewardedAdUsageStore(
            usage = RewardedAdUsage(
                localDate = "today",
                dailyRewardCount = 0,
                lastRewardEarnedAtMillis = 0L,
            ),
        )
        val manager = FakeRewardedAdManager(
            RewardedAdResult.Failure(AdsError(message = "load failed")),
        )
        val gatekeeper = gatekeeper(usageStore = store, rewardedAdManager = manager)

        val result = gatekeeper.showIfRequired(activity, RewardedAdPlacement.NEW_SCENE_SAVE)

        assertIs<RewardedAdResult.Failure>(result)
        assertEquals(1, manager.showCalls)
        assertEquals(0, store.recordCalls)
    }

    @Test
    fun showIfRequired_closedWithoutRewardDoesNotRecordReward() = runTest(dispatcher) {
        val store = FakeRewardedAdUsageStore(
            usage = RewardedAdUsage(
                localDate = "today",
                dailyRewardCount = 0,
                lastRewardEarnedAtMillis = 0L,
            ),
        )
        val manager = FakeRewardedAdManager(RewardedAdResult.ClosedWithoutReward)
        val gatekeeper = gatekeeper(usageStore = store, rewardedAdManager = manager)

        val result = gatekeeper.showIfRequired(activity, RewardedAdPlacement.NEW_SCENE_SAVE)

        assertEquals(RewardedAdResult.ClosedWithoutReward, result)
        assertEquals(1, manager.showCalls)
        assertEquals(0, store.recordCalls)
    }

    @Test
    fun usageStore_resetsCountOnLocalDateChange() = runTest(dispatcher) {
        val clock = FakeRewardedAdClock(nowMillis = 0L)
        val store = SharedPreferencesRewardedAdUsageStore(
            context = ApplicationProvider.getApplicationContext(),
            ioDispatcher = dispatcher,
            clock = clock,
        )
        store.clearDebugOnly()

        store.recordRewardEarned(RewardedAdPlacement.NEW_SCENE_SAVE, nowMillis = 0L)
        clock.nowMillis = 2L * 24L * 60L * 60L * 1000L

        val usage = store.getUsage(RewardedAdPlacement.NEW_SCENE_SAVE)

        assertEquals(0, usage.dailyRewardCount)
        assertEquals(0L, usage.lastRewardEarnedAtMillis)
    }

    private fun gatekeeper(
        clock: FakeRewardedAdClock = FakeRewardedAdClock(nowMillis = 20_000L),
        usageStore: RewardedAdUsageStore,
        rewardedAdManager: RewardedAdManager,
        config: AdsConfig = AdsConfig(),
    ): DefaultRewardedAdGatekeeper = DefaultRewardedAdGatekeeper(
        configProvider = FakeRewardedAdsConfigProvider(config),
        usageStore = usageStore,
        rewardedAdManager = rewardedAdManager,
        clock = clock,
    )
}

private class FakeRewardedAdsConfigProvider(private val config: AdsConfig) : AdsConfigProvider {
    override fun getConfig(): AdsConfig = config
}

private class FakeRewardedAdClock(var nowMillis: Long) : RewardedAdClock {
    override fun nowMillis(): Long = nowMillis
}

private class FakeRewardedAdUsageStore(private val usage: RewardedAdUsage) : RewardedAdUsageStore {
    var recordCalls = 0
    var lastRecordedNowMillis: Long? = null

    override suspend fun getUsage(placement: RewardedAdPlacement): RewardedAdUsage = usage

    override suspend fun recordRewardEarned(placement: RewardedAdPlacement, nowMillis: Long) {
        recordCalls += 1
        lastRecordedNowMillis = nowMillis
    }

    override suspend fun clearDebugOnly() = Unit
}

private class FakeRewardedAdManager(private val result: RewardedAdResult) : RewardedAdManager {
    var showCalls = 0

    override suspend fun show(activity: Activity, placement: RewardedAdPlacement): RewardedAdResult {
        showCalls += 1
        return result
    }
}
