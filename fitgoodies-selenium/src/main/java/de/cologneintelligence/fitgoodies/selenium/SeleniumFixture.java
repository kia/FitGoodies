/*
 * Copyright (c) 2002 Cunningham & Cunningham, Inc.
 * Copyright (c) 2009-2015 by Jochen Wierum & Cologne Intelligence
 *
 * This file is part of FitGoodies.
 *
 * FitGoodies is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FitGoodies is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FitGoodies.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cologneintelligence.fitgoodies.selenium;

import com.thoughtworks.selenium.SeleniumException;
import de.cologneintelligence.fitgoodies.ActionFixture;
import de.cologneintelligence.fitgoodies.htmlparser.FitCell;
import de.cologneintelligence.fitgoodies.runners.RunnerHelper;
import de.cologneintelligence.fitgoodies.selenium.command.CommandFactory;
import de.cologneintelligence.fitgoodies.util.DependencyManager;

import java.util.List;

/**
 * Run the selenium-IDE and record your test-case. Save the result as html and
 * copy the table part into a new Html-File. Adjust the first row and add the
 * reference to this class.
 * <table border="1" summary="">
 * <tr>
 * <td>fitgoodies.selenium.SeleniumFixture</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>open</td>
 * <td>/application/login.html</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>type</td>
 * <td>id_username</td>
 * <td>username</td>
 * </tr>
 * <tr>
 * <td>type</td>
 * <td>id_password</td>
 * <td>secret</td>
 * </tr>
 * <tr>
 * <td>clickAndWait</td>
 * <td>//input[@name='login']</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>clickAndWait</td>
 * <td>link=Products</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>clickAndRetry</td>
 * <td>link=Available Products</td>
 * <td></td>
 * </tr>
 * </table>
 *
 * @author kmussawisade
 */
public class SeleniumFixture extends ActionFixture {
    private int screenshotIndex = 0;

    @Override
    protected void doCells(List<FitCell> cells) {

        String command = cells.get(0).getFitValue();
        try {
            String arg1 = getColumnOrEmptyString(cells, 1);
            String arg2 = getColumnOrEmptyString(cells, 2);
            String[] args = new String[]{arg1, arg2};
            String result = CommandFactory.createCommand(command, args,
                DependencyManager.getOrCreate(SetupHelper.class)).execute();
            checkResult(last(cells), result);
        } catch (SeleniumException e) {
            wrong(last(cells), e.getMessage());
        } catch (Exception e) {
            last(cells).exception(e);
        }
    }

    public void wrong(FitCell cell, String message) {
        cell.wrong(message);
        SetupHelper helper = DependencyManager.getOrCreate(SetupHelper.class);
        if (helper.getTakeScreenshots()) {
            takeScreenShot(cell);
        }
    }

    private void addScreenshotLinkToReportPage(FitCell cell, String fileName) {
        cell.addDisplayValueRaw(" <a href=\"file:///" + fileName + "\">screenshot</a>");
    }

    private void takeScreenShot(FitCell cell) {
        String fileName = createSnapshotFilename(screenshotIndex++);
        SetupHelper helper = DependencyManager.getOrCreate(SetupHelper.class);
        CommandFactory.createCommand("captureEntirePageScreenshot", new String[]{fileName, ""}, helper).execute();
        addScreenshotLinkToReportPage(cell, fileName);
    }

    private String createSnapshotFilename(int index) {
        RunnerHelper helper = DependencyManager.getOrCreate(RunnerHelper.class);
        return helper.getResultFile() + ".screenshot" + index + ".png";
    }

    private String getColumnOrEmptyString(List<FitCell> cells, int column) {
        if (cells.size() <= column) {
            return "";
        } else {
            return validator.preProcess(cells.get(column).getFitValue());
        }
    }

    private void checkResult(FitCell cell, String result) {
        if (result.startsWith("OK")) {
            cell.right();
            cell.info(result);
        } else {
            wrong(cell, result);
        }
    }

    protected <T> T last(List<T> cells) {
        return cells.get(cells.size() - 1);
    }

}
