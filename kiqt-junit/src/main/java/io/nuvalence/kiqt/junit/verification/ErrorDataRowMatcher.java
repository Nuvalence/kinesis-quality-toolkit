package io.nuvalence.kiqt.junit.verification;

import io.nuvalence.kiqt.core.errors.AbstractErrorModel;

import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

/**
 * Custom matcher for matching only the contents of the {@link AbstractErrorModel#getDataRow()}.
 */
public class ErrorDataRowMatcher extends TypeSafeMatcher<AbstractErrorModel> {
    private Object expectedDataRow;

    /**
     * Creates a matcher given expected data row.
     *
     * @param expectedDataRow data row
     */
    public ErrorDataRowMatcher(Object expectedDataRow) {
        this.expectedDataRow = expectedDataRow;
    }

    @Override
    protected boolean matchesSafely(AbstractErrorModel item) {
        return Matchers.equalTo(expectedDataRow).matches(item.getDataRow());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("data row to match ").appendValue(expectedDataRow);
    }
}
