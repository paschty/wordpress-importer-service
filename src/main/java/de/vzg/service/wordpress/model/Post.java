/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vzg.service.wordpress.model;

import java.util.Map;

public class Post {

    private int id, author;

    private String date, date_gmt, modified, modified_gmt, slug, status, link, comment_status, ping_status, format;

    private PostContent title, content;

    private Map<Integer, Integer> categoeries, tags;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAuthor() {
        return author;
    }

    public void setAuthor(int author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateGmt() {
        return date_gmt;
    }

    public void setDateGmt(String date_gmt) {
        this.date_gmt = date_gmt;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getModifiedGmt() {
        return modified_gmt;
    }

    public void setModifiedGmt(String modified_gmt) {
        this.modified_gmt = modified_gmt;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCommentStatus() {
        return comment_status;
    }

    public void setCommentStatus(String comment_status) {
        this.comment_status = comment_status;
    }

    public String getPingStatus() {
        return ping_status;
    }

    public void setPingStatus(String ping_status) {
        this.ping_status = ping_status;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public PostContent getTitle() {
        return title;
    }

    public void setTitle(PostContent title) {
        this.title = title;
    }

    public PostContent getContent() {
        return content;
    }

    public void setContent(PostContent content) {
        this.content = content;
    }

    public Map<Integer, Integer> getCategoeries() {
        return categoeries;
    }

    public void setCategoeries(Map<Integer, Integer> categoeries) {
        this.categoeries = categoeries;
    }

    public Map<Integer, Integer> getTags() {
        return tags;
    }

    public void setTags(Map<Integer, Integer> tags) {
        this.tags = tags;
    }
}
