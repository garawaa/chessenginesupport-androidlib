/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kalab.chess.enginesupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;

public class EngineResolver {

	private static final String ENGINE_PROVIDER_MARKER = "intent.chess.provider.ENGINE";
	private static final String TAG = EngineResolver.class.getSimpleName();

	public List<Engine> resolveEngines(Context context) {
		List<Engine> result = new ArrayList<Engine>();
		final Intent intent = new Intent(ENGINE_PROVIDER_MARKER);
		List<ResolveInfo> list = context.getPackageManager()
				.queryIntentActivities(intent, PackageManager.GET_META_DATA);
		for (ResolveInfo resolveInfo : list) {
			String packageName = resolveInfo.activityInfo.packageName;
			result = resolveEnginesForPackage(context, result, resolveInfo,
					packageName);
		}
		return result;
	}

	private List<Engine> resolveEnginesForPackage(Context context,
			List<Engine> result, ResolveInfo resolveInfo, String packageName) {
		if (packageName != null) {
			Log.d(TAG, "found engine provider, packageName=" + packageName);
			Bundle bundle = resolveInfo.activityInfo.metaData;
			if (bundle != null) {
				String authority = bundle
						.getString("chess.provider.engine.authority");
				Log.d(TAG, "authority=" + authority);
				if (authority != null) {
					Integer resourceId = bundle
							.getInt("chess.provider.enginelist.xml");
					try {
						XmlResourceParser parser = context
								.getPackageManager()
								.getResourcesForApplication(
										resolveInfo.activityInfo.applicationInfo)
								.getXml(resourceId);
						parseEngineListXml(parser, authority, result);
					} catch (NotFoundException e) {
						Log.e(TAG, e.getLocalizedMessage(), e);
					} catch (NameNotFoundException e) {
						Log.e(TAG, e.getLocalizedMessage(), e);
					}
				}
			}
		}
		return result;
	}

	private void parseEngineListXml(XmlResourceParser parser, String authority,
			List<Engine> result) throws NameNotFoundException {
		try {
			int eventType = parser.getEventType();
			while (eventType != XmlResourceParser.END_DOCUMENT) {
				String name = null;
				try {
					if (eventType == XmlResourceParser.START_TAG) {
						name = parser.getName();
						if (name.equalsIgnoreCase("engine")) {
							String fileName = parser.getAttributeValue(null,
									"filename");
							Log.d(TAG, "filename=" + fileName);
							String title = parser.getAttributeValue(null,
									"name");
							Log.d(TAG, "name=" + title);
							result.add(new Engine(title, fileName, authority));
						}
					}
					eventType = parser.next();
				} catch (IOException e) {
					Log.e(TAG, e.getLocalizedMessage(), e);
				}
			}
		} catch (XmlPullParserException e) {
			Log.e(TAG, e.getLocalizedMessage(), e);
		}
	}
}
