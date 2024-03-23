package com.copytrading.model;

import com.copytrading.connector.model.PositionDto;
import com.copytrading.sources.copytradingleaderboard.model.response.positions.active.PositionData;
import com.copytrading.sources.futuresleaderboard.model.response.position.Position;

import java.util.List;

import static com.copytrading.sources.futuresleaderboard.FuturesLeaderboardScrapper.getTopTradersPositions;
import static java.lang.Double.parseDouble;

public enum OrderSide {
    BUY,
    SELL;

    /**
     * Returns main order side of positions, for example from sell, sell, buy, sell - main side is sell.
     * @return order side
     */
    public static OrderSide getMainOrderSide(int limit) {
        List<Position> positions = getTopTradersPositions(limit);
        int sellSideCount = 0;
        int buySideCount = 0;
        for (Position position : positions) {
            OrderSide side = getPositionSide(position);
            if (side.equals(BUY)) {
                buySideCount++;
            } else {
                sellSideCount++;
            }
        }
        if (buySideCount > sellSideCount) {
            return BUY;
        } else if (sellSideCount > buySideCount) {
            return SELL;
        } else {
            limit += 5;
            return getMainOrderSide(limit);
        }
    }

    public static OrderSide getPositionSide(Position position) {
        double entry = position.getEntryPrice();
        double mark = position.getMarkPrice();
        double unrealized = position.getPnl();
        if ((mark > entry && unrealized > 0) || (mark < entry && unrealized < 0))
            return OrderSide.BUY;
        else
            return OrderSide.SELL;
    }

    public static OrderSide getPositionSide(PositionData positionData) {
        if (positionData.getPositionSide().equals(PositionSide.BOTH)) {
            double entry = parseDouble(positionData.getEntryPrice());
            double mark = parseDouble(positionData.getMarkPrice());
            double unrealized = parseDouble(positionData.getUnrealizedProfit());
            if ((mark > entry && unrealized > 0) || (mark < entry && unrealized < 0))
                return OrderSide.BUY;
            else
                return OrderSide.SELL;
        } else {
            return positionData.getPositionSide().equals(PositionSide.LONG) ? BUY : SELL;
        }
    }

    public static OrderSide getPositionSide(PositionDto positionDto) {
        double entry = positionDto.getEntryPrice();
        double mark = positionDto.getMarkPrice();
        double unrealized = positionDto.getUnRealizedProfit();
        if ((mark > entry && unrealized > 0) || (mark < entry && unrealized < 0))
            return OrderSide.BUY;
        else
            return OrderSide.SELL;
    }

    public static String getOppositeSide(PositionDto positionDto) {
        OrderSide side = getPositionSide(positionDto);
        if (side.equals(BUY))
            return SELL.name();
        else return BUY.name();
    }
}
