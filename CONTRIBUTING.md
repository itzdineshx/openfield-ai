# Contributing to OpenField

Thank you for considering a contribution.

## Before starting

1. Read the Phase 1 PRD and relevant architecture/API documents.
2. Search existing issues and discussions.
3. For a substantial feature, open an issue first to confirm scope and design.
4. Avoid adding features outside Phase 1 without an approved scope decision.

## Development rules

- Keep local AI and local content usable without a backend.
- Do not transmit prompts, documents, images, or chat history unless the user explicitly initiates a clearly disclosed action.
- Do not log sensitive content.
- Keep UI code in Compose; use ViewModels/use cases/repositories for behavior.
- Isolate model/runtime APIs behind an adapter.
- Make long-running work cancellable and keep it off the main thread.
- Add tests for behavior and edge cases.
- Include loading, empty, offline, failure, cancellation, and retry states where applicable.
- Use accessible labels and scalable text.
- Verify AI/runtime features on a physical device.

## Pull requests

A PR should include:
- What changed and why
- Linked issue
- Screenshots/recording for UI changes
- Tests performed and device/environment
- Privacy/network impact
- Known limitations

Keep PRs focused. Do not combine unrelated refactors and features.

## Commit messages

Use a consistent convention, for example:
- `feat(chat): persist conversation history`
- `fix(ai): handle model initialization failure`
- `docs: clarify offline behavior`
- `test(documents): cover chunk boundaries`

## Reporting bugs

Include app version/commit, Android version/device, steps to reproduce, expected vs actual behavior, logs with sensitive content removed, and whether the issue occurs offline.

Never attach private documents, credentials, personal images, or unredacted prompts to public issues.
