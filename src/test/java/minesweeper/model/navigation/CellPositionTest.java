package minesweeper.model.navigation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CellPositionTest {

    // �������� ��������� ���������, ����� ��������������� �� ����� ������,
    // ��� ��� ��� ����������� � ������ �� ��� �����.
    private static CellRange originalHorizontalRange;
    private static CellRange originalVerticalRange;

    @BeforeEach
    void setUpGlobalRanges() {
        // ��������� ������� ���������� ��������� ����� ������ ������
        originalHorizontalRange = CellPosition.horizontalRange();
        originalVerticalRange = CellPosition.verticalRange();

        // ������������� ��������� ��������� ��� ����������� ������
        CellPosition.setHorizontalRange(0, 9); // ��������, ���� 10x10
        CellPosition.setVerticalRange(0, 9);
    }

    @AfterEach
    void restoreGlobalRanges() {
        // ��������������� �������� ���������� ��������� ����� ������� �����
        // ����� ����� �� ������ ���� �� ����� ����� ����������� ����.
        CellPosition.setHorizontalRange(originalHorizontalRange.min(), originalHorizontalRange.max());
        CellPosition.setVerticalRange(originalVerticalRange.min(), originalVerticalRange.max());
    }

    @Test
    @DisplayName("����������� ������� CellPosition � ����������� row � column")
    void constructor_setsRowAndColumnCorrectly() {
        CellPosition pos = new CellPosition(3, 5);
        assertEquals(3, pos.getRow());
        assertEquals(5, pos.getColumn());
    }

    @Test
    @DisplayName("getRow � getColumn ���������� ���������� ��������")
    void getters_returnCorrectValues() {
        int r = 7, c = 2;
        CellPosition pos = new CellPosition(r, c);
        assertEquals(r, pos.getRow());
        assertEquals(c, pos.getColumn());
    }

    @Test
    @DisplayName("setHorizontalRange ������������� �������������� ��������")
    void setHorizontalRange_updatesRange() {
        CellPosition.setHorizontalRange(1, 5);
        assertEquals(1, CellPosition.horizontalRange().min());
        assertEquals(5, CellPosition.horizontalRange().max());
    }

    @Test
    @DisplayName("setHorizontalRange �� ������ �������� ��� ���������� min/max")
    void setHorizontalRange_invalidRange_doesNotUpdate() {
        CellPosition.setHorizontalRange(0, 9); // ���������
        CellRange initialRange = CellPosition.horizontalRange();

        CellPosition.setHorizontalRange(5, 1); // ���������� (max < min)
        assertEquals(initialRange.min(), CellPosition.horizontalRange().min(), "�������� �� ������ ��� ����������");
        assertEquals(initialRange.max(), CellPosition.horizontalRange().max(), "�������� �� ������ ��� ����������");

        // CellRange ��� ������������ min < 0, ������������ ��� � 0.
        // CellRange.isValidRange ��������� min >= 0 && max >= min.
        CellPosition.setHorizontalRange(-1, 5); // ���������� �� CellRange.isValidRange
        // ���������, ��� �� ��������� ������������ ��������� *�����* ���������� ������� (�.�. 0,9)
        assertEquals(initialRange.min(), CellPosition.horizontalRange().min());
        assertEquals(initialRange.max(), CellPosition.horizontalRange().max());
    }

    @Test
    @DisplayName("setVerticalRange ������������� ������������ ��������")
    void setVerticalRange_updatesRange() {
        CellPosition.setVerticalRange(2, 7);
        assertEquals(2, CellPosition.verticalRange().min());
        assertEquals(7, CellPosition.verticalRange().max());
    }

    @ParameterizedTest
    @CsvSource({
            "0,0,true",   // ������ ��������� 0-9, 0-9
            "9,9,true",   // ������� ���������
            "5,5,true",   // ��������
            "0,10,false", // Column �� ���������
            "10,0,false", // Row �� ���������
            "-1,5,false", // Row < 0
            "5,-1,false"  // Column < 0
    })
    @DisplayName("isValid (�����������) ���������, ��������� �� ������� � ������������� ����������")
    void staticIsValid_checksPositionAgainstRanges(int r, int c, boolean expected) {
        assertEquals(expected, CellPosition.isValid(r, c));
    }

    @Test
    @DisplayName("isValid (����� ����������) ��������� ���������� ������� �������")
    void instanceIsValid_checksOwnPosition() {
        CellPosition validPos = new CellPosition(1, 1);
        assertTrue(validPos.isValid());

        CellPosition.setHorizontalRange(0,0); // ������ ��������
        CellPosition.setVerticalRange(0,0);

        CellPosition nowInvalidPos = new CellPosition(1,1); // ��� ������� ������ ���������
        assertFalse(nowInvalidPos.isValid());

        CellPosition stillValidPos = new CellPosition(0,0);
        assertTrue(stillValidPos.isValid());
    }

    @Test
    @DisplayName("equals ������ ���������� true ��� ���������� ������� � false ��� ������")
    void equals_comparesPositionsCorrectly() {
        CellPosition pos1 = new CellPosition(2, 3);
        CellPosition pos2 = new CellPosition(2, 3);
        CellPosition pos3 = new CellPosition(3, 2);
        CellPosition pos4 = new CellPosition(2, 4);

        assertEquals(pos1, pos2, "������� � ����������� (r,c) ������ ���� �����");
        assertNotEquals(pos1, pos3, "������� � ������� (r,c) �� ������ ���� �����");
        assertNotEquals(pos1, pos4, "������� � ������� (r,c) �� ������ ���� �����");
        assertNotEquals(pos1, null, "��������� � null ������ ���������� false");
        assertNotEquals(pos1, new Object(), "��������� � �������� ������� ���� ������ ���������� false");
        assertEquals(pos1,pos1,"������ ������ ���� ����� ������ ����");
    }

    @Test
    @DisplayName("hashCode ������ ���� ���������� ��� ������ ��������")
    void hashCode_isConsistentForEqualObjects() {
        CellPosition pos1 = new CellPosition(5, 8);
        CellPosition pos2 = new CellPosition(5, 8);
        assertEquals(pos1.hashCode(), pos2.hashCode(), "���-���� ������ �������� ������ ���������");
    }

    @Test
    @DisplayName("hashCode ������ ����������� ��� �� ������ ��������")
    void hashCode_usuallyDiffersForUnequalObjects() {
        CellPosition pos1 = new CellPosition(1, 2);
        CellPosition pos3 = new CellPosition(2, 1);
        // ��� �� ������� ���������� ��� hashCode, �� ������ �����������
        assertNotEquals(pos1.hashCode(), pos3.hashCode(), "���-���� ������ �������� ������ �����������");
    }


    @Test
    @DisplayName("toString ���������� ���������� ��������� �������������")
    void toString_returnsCorrectFormat() {
        CellPosition pos = new CellPosition(4, 6);
        assertEquals("CellPosition(4, 6)", pos.toString());
    }
}