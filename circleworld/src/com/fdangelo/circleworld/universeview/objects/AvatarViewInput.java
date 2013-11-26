package com.fdangelo.circleworld.universeview.objects;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.fdangelo.circleworld.GameLogic;
import com.fdangelo.circleworld.GameLogicState;
import com.fdangelo.circleworld.universeengine.objects.Avatar;
import com.fdangelo.circleworld.universeengine.objects.AvatarInput;
import com.fdangelo.circleworld.universeview.UniverseViewCamera;
import com.fdangelo.circleworld.utils.Vector2I;

public class AvatarViewInput {
	private final AvatarView avatarView;

	static public AvatarInputMode mode = AvatarInputMode.Move;
	static public AvatarInputEditTool editTool = AvatarInputEditTool.None;

	/*
	 * static private String[] EditToolNames = new String[] { "None",
	 * "Add Tiles", "Remove Tiles", "Move Camera" }; static private String[]
	 * EditToolTooltips = new String[] { "Select a tool",
	 * "Tap on empty spaces to add tiles", "Tap on tiles to remove them",
	 * "Move the camera" };
	 */

	static private Vector3 tmpv = new Vector3();
	static private Vector2I tmpvi = new Vector2I();

	public AvatarViewInput(final AvatarView avatarView) {
		this.avatarView = avatarView;
	}

	// private GUIStyle centeredLabelStyle;
	// private GUIStyle centeredBoxStyle;

	public final void update(final float deltaTime) {
		if (GameLogic.instace.getState() != GameLogicState.PlayingAvatar) {
			return;
		}

		switch (mode) {
			case Edit:
				updateTilesModification();
				UniverseViewCamera.instance.updateZoomInput(deltaTime);
				break;

			case Move:
				updateWalkAndJump();
				UniverseViewCamera.instance.updateZoomInput(deltaTime);
				break;

			case TravelToPlanet:
				UniverseViewCamera.instance.updateZoomInput(deltaTime);
				UniverseViewCamera.instance.updateClickOnPlanetToTravel(avatarView.getUniverseView());
				break;
		}
	}

	private final void updateWalkAndJump() {
		final AvatarInput avatarInput = ((Avatar) avatarView.getUniverseObject()).input;

		if (Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS) {
			int touchCount = 0;
			int touch1x = -1, touch1y = -1;
			int touch2x = -1, touch2y = -1;

			if (Gdx.input.isTouched(0)) {
				touch1x = Gdx.input.getX(0);
				touch1y = Gdx.input.getY(0);
				touchCount++;
			}

			if (Gdx.input.isTouched(1)) {
				touch2x = Gdx.input.getX(1);
				touch2y = Gdx.input.getY(1);
				touchCount++;
			}

			final float screenWidth = Gdx.graphics.getWidth();
			final float screenHeight = Gdx.graphics.getHeight();

			if (touchCount >= 1) {
				if (touch1x > screenWidth / 2.0f && touch1y > screenHeight * 0.75f || touchCount > 1 && touch2x > screenWidth / 2.0f
						&& touch2y > screenHeight * 0.75f) {
					avatarInput.jump = true;
				}

				if (touch1x < screenWidth / 4.0f && touch1y > screenHeight * 0.75f || touchCount > 1 && touch2x < screenWidth / 4.0f
						&& touch2y > screenHeight * 0.75f) {
					avatarInput.walkDirection = -1.0f;
				} else if (touch1x < screenWidth / 2.0f && touch1y > screenHeight * 0.75f || touchCount > 1 && touch2x < screenWidth / 2.0f
						&& touch2y > screenHeight * 0.75f) {
					avatarInput.walkDirection = 1.0f;
				}
			}
		} else {
			if (Gdx.input.isKeyPressed(Input.Keys.A)) {
				avatarInput.walkDirection = -1;
			} else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
				avatarInput.walkDirection = 1;
			} else {
				avatarInput.walkDirection = 0;
			}

			avatarInput.jump = Gdx.input.isKeyPressed(Input.Keys.SPACE);

			// if (Gdx.input.isKeyPressed(Input.Keys.T))
			// GameLogic.Instace.PlayerBoardShip();
		}
	}

	public final void updateTilesModification() {
		boolean modifyTile = false;
		int tileX = 0;
		final int tileY = 0;

		if (Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS) {
			if (Gdx.input.isTouched(0) && !Gdx.input.isTouched(1) && !InputAreas.isInputArea(Gdx.input.getX(), Gdx.input.getX())) {
				modifyTile = getTileCoordinatesUnderTouch(tmpvi);
				tileX = tmpvi.x;
				tileX = tmpvi.y;
			}
		} else {
			if (Gdx.input.justTouched() && !InputAreas.isInputArea(Gdx.input.getX(), Gdx.input.getX())) {
				modifyTile = getTileCoordinatesUnderMouse(tmpvi);
				tileX = tmpvi.x;
				tileX = tmpvi.y;
			}
		}

		switch (editTool) {
			case Add:
				if (modifyTile) {
					avatarView.getParentView().getTilemapCircle().getTile(tileX, tileY, (byte) 1);
				}
				break;

			case Remove:
				if (modifyTile) {
					avatarView.getParentView().getTilemapCircle().getTile(tileX, tileY, (byte) 0);
				}
				break;

			case MoveCamera:
				UniverseViewCamera.instance.updateMove();
				break;

			case None:
				break;
		}
	}

	private final boolean getTileCoordinatesUnderMouse(final Vector2I tileCoordinates) {
		final Camera cam = UniverseViewCamera.instance.getCamera();

		tmpv.x = Gdx.input.getX();
		tmpv.y = Gdx.input.getY();

		cam.unproject(tmpv);

		return avatarView.getParentView().getTilemapCircle().getTileCoordinatesFromPosition(tmpv.x, tmpv.y, tileCoordinates);
	}

	private final boolean getTileCoordinatesUnderTouch(final Vector2I tileCoordinates) {
		final Camera cam = UniverseViewCamera.instance.getCamera();

		tmpv.x = Gdx.input.getX();
		tmpv.y = Gdx.input.getY();

		cam.unproject(tmpv);

		return avatarView.getParentView().getTilemapCircle().getTileCoordinatesFromPosition(tmpv.x, tmpv.y, tileCoordinates);
	}

	/*
	 * #if ENABLE_ONGUI public void OnGUI() { if (GameLogic.Instace.State !=
	 * GameLogicState.PlayingAvatar) return; if (centeredLabelStyle == null) {
	 * centeredLabelStyle = new GUIStyle(GUI.skin.label);
	 * centeredLabelStyle.alignment = TextAnchor.MiddleCenter; centeredBoxStyle
	 * = new GUIStyle(GUI.skin.box); centeredBoxStyle.alignment =
	 * TextAnchor.MiddleCenter; } InputAreas.ResetInputAreas(); switch(mode) {
	 * case AvatarInputMode.Move: DrawMoveGUI(); break; case
	 * AvatarInputMode.Edit: DrawEditGUI(); break; case
	 * AvatarInputMode.TravelToPlanet: DrawTravelToPlanetGUI(); break; } }
	 * private void DrawEditGUI() { //Draw toolbar InputAreas.AddInputArea(new
	 * Rect(0, Screen.height - Screen.height * 0.25f, touch2, Screen.height *
	 * 0.25f)); editTool = (AvatarInputEditTool) GUI.Toolbar(new Rect(0,
	 * Screen.height - Screen.height * 0.25f, Screen.width, Screen.height *
	 * 0.25f - 50), (int) editTool, EditToolNames); GUI.Box(new Rect(0,
	 * Screen.height - 50, Screen.width, 50), EditToolTooltips[(int) editTool],
	 * centeredBoxStyle); //Draw cancel button InputAreas.AddInputArea(new
	 * Rect(Screen.width - Screen.width / 8, 0, Screen.width / 8, Screen.height
	 * / 8)); if (GUI.Button(new Rect(Screen.width - Screen.width / 8, 0,
	 * Screen.width / 8, Screen.height / 8), "EXIT\nEDIT")) mode =
	 * AvatarInputMode.Move; } private void DrawTravelToPlanetGUI() { //Draw
	 * cancel button InputAreas.AddInputArea(new Rect(Screen.width -
	 * Screen.width / 8, 0, Screen.width / 8, Screen.height / 8)); if
	 * (GUI.Button(new Rect(Screen.width - Screen.width / 8, 0, Screen.width /
	 * 8, Screen.height / 8), "EXIT\nTRAVEL")) mode = AvatarInputMode.Move;
	 * GUI.Box(new Rect(0, Screen.height - 50, Screen.width, 50),
	 * "Tap on a planet to travel", centeredBoxStyle); } private void
	 * DrawMoveGUI() { //Draw movement keys if (Application.platform ==
	 * RuntimePlatform.Android || Application.platform ==
	 * RuntimePlatform.IPhonePlayer) { InputAreas.AddInputArea(new Rect(0,
	 * Screen.height - Screen.height * 0.25f, Screen.width, Screen.height *
	 * 0.25f)); GUI.Button(new Rect(0, Screen.height - Screen.height * 0.25f,
	 * Screen.width / 4, Screen.height * 0.25f), "Left"); GUI.Button(new
	 * Rect(Screen.width / 4.0f, Screen.height - Screen.height * 0.25f,
	 * Screen.width / 4, Screen.height * 0.25f), "Right"); GUI.Button(new
	 * Rect(Screen.width / 2.0f, Screen.height - Screen.height * 0.25f,
	 * Screen.width / 2, Screen.height * 0.25f), "Jump"); } //Draw travel button
	 * //InputAreas.AddInputArea(new Rect(Screen.width - (Screen.width / 8) *
	 * 2.0f, 0, Screen.width / 8, Screen.height / 8)); //if (GUI.Button(new
	 * Rect(Screen.width - (Screen.width / 8) * 2.0f, 0, Screen.width / 8,
	 * Screen.height / 8), "TRAVEL")) // mode = AvatarInputMode.TravelToPlanet;
	 * //Draw edit button InputAreas.AddInputArea(new Rect(Screen.width -
	 * Screen.width / 8, 0, Screen.width / 8, Screen.height / 8)); if
	 * (GUI.Button(new Rect(Screen.width - Screen.width / 8, 0, Screen.width /
	 * 8, Screen.height / 8), "EDIT")) { mode = AvatarInputMode.Edit; editTool =
	 * AvatarInputEditTool.None; } //Draw switch to ship button
	 * InputAreas.AddInputArea(new Rect(Screen.width - (Screen.width / 8) *
	 * 3.0f, 0, Screen.width / 8, Screen.height / 8)); if (GUI.Button(new
	 * Rect(Screen.width - (Screen.width / 8) * 3.0f, 0, Screen.width / 8,
	 * Screen.height / 8), "BOARD SHIP")) GameLogic.Instace.PlayerBoardShip(); }
	 * #endif
	 */

}
